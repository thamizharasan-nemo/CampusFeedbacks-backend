package com.feedbacks.FeedbackSystem.service.serviceImple;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.PageResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.FeedbackRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseFeedbackSummary;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.FeedbackResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbackTrendDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.RatingDistributionDTO;
import com.feedbacks.FeedbackSystem.Exception.NotAllowedException;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.configure.FeedbackMetrics;
import com.feedbacks.FeedbackSystem.mapper.FeedbackMapper;
import com.feedbacks.FeedbackSystem.model.*;
import com.feedbacks.FeedbackSystem.repository.EnrollmentRepository;
import com.feedbacks.FeedbackSystem.repository.FeedbackRepository;
import com.feedbacks.FeedbackSystem.security.FeedbackRateLimiterService;
import com.feedbacks.FeedbackSystem.security.SecurityUtils;
import com.feedbacks.FeedbackSystem.service.interfaces.FeedbackService;
import com.feedbacks.FeedbackSystem.specification.FeedbackSpecification;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepo;
    private final UserServiceImpl userService;
    private final CourseServiceImpl courseService;
    private final EnrollmentRepository enrollmentRepo;
    private final FeedbackMapper feedbackMapper;
    private final InstructorServiceImpl instructorService;
    private final FeedbackRateLimiterService rateLimiterService;
    private final FeedbackMetrics feedbackMetrics;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepo,
                               UserServiceImpl userService,
                               CourseServiceImpl courseService,
                               EnrollmentRepository enrollmentRepo,
                               FeedbackMapper feedbackMapper,
                               InstructorServiceImpl instructorService,
                               FeedbackRateLimiterService rateLimiterService,
                               FeedbackMetrics feedbackMetrics) {
        this.feedbackRepo = feedbackRepo;
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentRepo = enrollmentRepo;
        this.feedbackMapper = feedbackMapper;
        this.instructorService = instructorService;
        this.rateLimiterService = rateLimiterService;
        this.feedbackMetrics = feedbackMetrics;
    }

    public int getCurrentUserId(){
        return SecurityUtils.getCurrentUserId();
    }

    public Feedback getFeedbackById(Integer feedbackId) {
        return feedbackRepo.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found! Id: " + feedbackId));
    }

    @Override
    public FeedbackResponseDTO getFeedbackResponseById(int feedbackId) {
        return feedbackMapper.toResponse(getFeedbackById(feedbackId));
    }

    public Page<FeedbackResponseDTO> getAllFeedbacks(int page, int size, String sort){
        Pageable pageable = PageRequest.of(page, size, sortingFunction(sort));
        User currentUser = userService.getUserById(getCurrentUserId());
        Page<Feedback> feedbacks =  feedbackRepo.findAllFeedbacks(currentUser.getInstitution().getInstitutionId(), pageable);
        return feedbacks.map(feedbackMapper::toResponse);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @Override
    public List<FeedbackResponseDTO> getAllFeedbacksByInstituteId() {
        int institutionId = userService.getUserById(getCurrentUserId()).getInstitution().getInstitutionId();
        return feedbackRepo.getAllFeedbacksByInstituteId(institutionId)
                .stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validateInstitutionConsistency(User student,
                                                Course course,
                                                Instructor instructor) {
        int studentInstitutionId = student.getInstitution().getInstitutionId();
        int courseInstitutionId = course.getInstitution().getInstitutionId();
        int instructorInstitutionId = instructor.getUser().getInstitution().getInstitutionId();

        if (studentInstitutionId != courseInstitutionId || courseInstitutionId != instructorInstitutionId) {
            throw new NotAllowedException("Cross-institution feedback is not allowed");
        }
    }

    private int getStudentInstitutionId(int studentId) {
        User student = userService.getUserById(studentId);
        return student.getInstitution().getInstitutionId();
    }

    @Override
    @CacheEvict(value = {
            "feedbackTrends",
            "feedbackDistribution",
            "popularCourses",
            "unpopularCourses",
            "courseRanking",
            "TopInstructors"
    }, allEntries = true)
    @Transactional
    public FeedbackResponseDTO submitFeedback(@Valid FeedbackRequestDTO feedbackRequestDTO) {
        int currentUserId = getCurrentUserId();

        rateLimiterService.checkRateLimit(currentUserId); // limits users feedbacks

        User student = userService.getUserById(currentUserId);
        if (!student.getRole().equals(User.Role.STUDENT)) {
            throw new NotAllowedException("Only students can submit feedback.");
        }

        Course course = courseService.getCourseById(feedbackRequestDTO.getCourseId());

        if (!enrollmentRepo.existsByCourse_CourseIdAndStudent_UserIdAndInstitution_InstitutionId(
                course.getCourseId(),
                currentUserId,
                student.getInstitution().getInstitutionId()
        )) {
            throw new NotAllowedException("Student " + student.getUsername() + " is not enrolled in course " + course.getCourseName());
        }

        Instructor instructor = instructorService.getInstructorById(feedbackRequestDTO.getInstructorId());
        if (instructor == null) throw new ResourceNotFoundException("Instructor not found");

        validateInstitutionConsistency(student, course, instructor);

        Feedback feedback = new Feedback();
        feedbackMapper.toEntity(feedbackRequestDTO, feedback, student, course, instructor, student.getInstitution());
        feedbackRepo.save(feedback);

        feedbackMetrics.incrementFeedbackSubmittedCount();
        courseService.updateCourseStateOnFeedbackAdd(course, feedback.getCourseRating(), false);
        instructorService.updateInstructorStateOnFeedbackAdd(instructor, feedback.getInstructorRating(), false);

        log.info("FEEDBACK_SUBMITTED userId={} courseId={} rating={}", currentUserId, course.getCourseId(), feedback.getCourseRating());
        return feedbackMapper.toResponse(feedback);
    }


    @Override
    @CacheEvict(value = {
            "feedbackTrends",
            "feedbackDistribution",
            "popularCourses",
            "unpopularCourses",
            "courseRanking",
            "TopInstructors"
    }, allEntries = true)
    public FeedbackResponseDTO editFeedback(int feedbackId, @Valid FeedbackRequestDTO feedbackRequestDTO) {
        int currentUserId = getCurrentUserId();
        Feedback feedback = feedbackRepo.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));

        if (feedback.getStudent().getUserId() != currentUserId) {
            throw new NotAllowedException("You can only edit your own feedback");
        }

        Course course = courseService.getCourseById(feedbackRequestDTO.getCourseId());
        Instructor instructor = instructorService.getInstructorById(feedbackRequestDTO.getInstructorId());
        validateInstitutionConsistency(feedback.getStudent(), course, instructor);

        feedbackMapper.toEntity(feedbackRequestDTO, feedback, feedback.getStudent(), course, instructor, feedback.getStudent().getInstitution());
        feedbackRepo.save(feedback);

        courseService.updateCourseStateOnFeedbackAdd(course, feedback.getCourseRating(), true);
        instructorService.updateInstructorStateOnFeedbackAdd(instructor, feedback.getInstructorRating(), true);

        log.info("FEEDBACK_EDITED userId={} courseId={} rating={}", currentUserId, course.getCourseId(), feedback.getCourseRating());
        return feedbackMapper.toResponse(feedback);
    }


    @Override
    @Transactional
    public void deleteFeedbackById(int feedbackId) {
        int currentUserId = getCurrentUserId();
        Feedback feedback = feedbackRepo.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));

        User currentUser = userService.getUserById(currentUserId);

        if (feedback.getStudent().getUserId() == currentUserId || currentUser.getRole() == User.Role.ADMIN) {

            feedback.setDeleted(true);
            feedback.setDeletedAt(LocalDateTime.now());
            feedback.setDeletedBy(SecurityContextHolder.getContext().getAuthentication().getName());
            feedbackRepo.save(feedback);

            courseService.updateCourseStateOnFeedbackRemove(feedback.getCourse(), feedback.getCourseRating());
            instructorService.updateInstructorStateOnFeedbackRemove(feedback.getInstructor(), feedback.getInstructorRating());

            log.info("FEEDBACK_DELETED userId={} feedbackId={}", currentUserId, feedbackId);
            return;
        }
        throw new NotAllowedException("You can only delete your own feedback");
    }


    @Override
    public List<FeedbackResponseDTO> findAllSoftDeletedFeedbacks(){
        User currentUser = userService.getUserById(getCurrentUserId());
        List<Feedback> deletedFeedbacks = feedbackRepo.findAllDeletedFeedback(currentUser.getInstitution().getInstitutionId());
        if(deletedFeedbacks.isEmpty()){
            return new ArrayList<>();
        }
        return deletedFeedbacks.stream()
                .map(feedbackMapper::toResponse)
                .toList();
    }

    @Override
    public FeedbackResponseDTO restoreFeedback(int feedbackId){
        Feedback feedback = getFeedbackById(feedbackId);
        User accessor = userService.getUserById(getCurrentUserId());

        if (feedback.getStudent().getUserId() != accessor.getUserId()
                && accessor.getRole() == User.Role.STUDENT){
            throw new NotAllowedException("You can not restore someone's feedback");
        }

        feedback.setDeleted(false);
        feedback.setDeletedBy(null);
        feedback.setDeletedAt(null);
        feedback.setRestoredBy(SecurityContextHolder.getContext().getAuthentication().getName());

        log.info(
                "event=FEEDBACK_RESTORED studentId={} feedbackId={} restoredBy={}",
                accessor.getUserId(), feedbackId, accessor.getUsername()
        );

        feedbackRepo.save(feedback);

        courseService.updateCourseStateOnFeedbackRemove(feedback.getCourse(), feedback.getCourseRating());
        instructorService.updateInstructorStateOnFeedbackRemove(feedback.getCourse().getInstructor(), feedback.getInstructorRating());

        return feedbackMapper.toResponse(feedback);
    }

    @Transactional
    @Override
    public void deleteFeedbackPermanently(int feedbackId){
        if(getFeedbackById(feedbackId) == null){
            throw new ResourceNotFoundException("Feedback not found");
        }

        Feedback feedback = getFeedbackById(feedbackId);

        User accessor = userService.getUserById(getCurrentUserId());

        if (feedback.getStudent().getUserId() != accessor.getUserId()
                && accessor.getRole() == User.Role.STUDENT){
            throw new NotAllowedException("You can not delete someone's feedback");
        }

        log.info(
                "event=FEEDBACK_PERMANENTLY_DELETED studentId={} feedbackId={} deletedBy={}",
                accessor.getUserId(), feedbackId, accessor.getUsername()
        );

        courseService.updateCourseStateOnFeedbackRemove(feedback.getCourse(), feedback.getCourseRating());
        instructorService.updateInstructorStateOnFeedbackRemove(feedback.getCourse().getInstructor(), feedback.getInstructorRating());

        feedbackRepo.deletePermanently(feedbackId, accessor.getInstitution().getInstitutionId());
    }

    @Override
    public PageResponseDTO<FeedbackResponseDTO> getAllByInstructorId(int instructorId, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<Feedback> feedbacks = feedbackRepo
                .findAllByInstructor_InstructorIdAndInstitutionId(
                        instructorId,
                        SecurityUtils.getInstitutionId(),
                        pageable
                );

        Page<FeedbackResponseDTO> feedbackResponseDTOS =  feedbacks.map(feedback -> feedbackMapper.toResponse(feedback));

        return new PageResponseDTO<>(
                feedbackResponseDTOS.getContent(),
                feedbackResponseDTOS.getNumber(),
                feedbackResponseDTOS.getSize(),
                feedbackResponseDTOS.getTotalElements(),
                feedbackResponseDTOS.getTotalPages(),
                feedbackResponseDTOS.isLast()
        );
    }

    //Sorting Method
    @Override
    public Sort sortingFunction(String sort){
        String[] sortParam = sort.split(","); //Split the string by ',' store them in an array
        String sortBy = sortParam[0];   // sorting order is 0th value which is field name
        Sort.Direction direction = Sort.Direction.fromString(sortParam.length > 1 ? sortParam[1] : "ASC"); //sorting direction is 1st value
        return Sort.by(direction, sortBy);
    }

    @Override
    public Page<FeedbackResponseDTO> getSortedFeedbackByCourseId(int courseId, int page, int size, String sort) {
        Course course = courseService.getCourseById(courseId);
        int institutionId = course.getInstitution().getInstitutionId();

        Sort sorting = sortingFunction(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Feedback> feedbacks = feedbackRepo.findByCourse_CourseIdAndInstitution_InstitutionId(courseId, institutionId, pageable);
        return feedbacks.map(feedbackMapper::toResponse);
    }


    @Override
    public List<FeedbackResponseDTO> getFilteredFeedback(Integer courseId, Integer minRating, Integer maxRating,
                                                         LocalDateTime fromDate, LocalDateTime toDate, Boolean anonymous) {
        int institutionId = courseId != null
                ? courseService.getCourseById(courseId).getInstitution().getInstitutionId()
                : 0;

        List<Feedback> feedbacks = feedbackRepo.filterFeedback(
                courseId, minRating, maxRating, fromDate, toDate, anonymous, institutionId
        );

        return feedbacks.stream()
                .map(feedbackMapper::toResponse)
                .toList();
    }

    @Override
    public PageResponseDTO<FeedbackResponseDTO> getFeedbackByStudent(int page, int size, String sort) {
        Sort sorting = sortingFunction(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Feedback> feedbacks = feedbackRepo
                .findByStudent_UserIdAndInstitution_InstitutionId(
                        SecurityUtils.getCurrentUserId(),
                        SecurityUtils.getInstitutionId(),
                        pageable
                );
        Page<FeedbackResponseDTO> dtoPage = feedbacks.map(feedbackMapper::toResponse);
        return new PageResponseDTO<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.isLast()
        );
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Double getAverageCourseRating(int courseId){
        User currentUser = userService.getUserById(getCurrentUserId());
        Double avgRating = feedbackRepo.getAvgCourseRating(courseId, currentUser.getInstitution().getInstitutionId());
        if(avgRating == null) {
            throw new ResourceNotFoundException("No average rating to this course.");
        }
        return avgRating;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Double getAverageInstructorRating(int instructorId){
        User currentUser = userService.getUserById(getCurrentUserId());
        Double avgRating = feedbackRepo.getAvgInstructorRating(instructorId, currentUser.getInstitution().getInstitutionId());
        if(avgRating == null) {
            throw new ResourceNotFoundException("No average rating to this instructor.");
        }
        return avgRating;
    }

    @Override
    public List<CourseFeedbackSummary> getCourseSummary() {
        return feedbackRepo.findCourseSummariesByInstitution(getStudentInstitutionId(
                getCurrentUserId()
        ));
    }

    @Override
    public List<FeedbackResponseDTO> getFeedbacksByStudentAndCourse(int courseId) {
        User student = userService.getUserById(getCurrentUserId());
        List<Feedback> feedbacks = feedbackRepo
                .findByStudent_UserIdAndCourse_CourseIdAndInstitution_InstitutionId(student.getUserId(),
                        courseId,
                        student.getInstitution().getInstitutionId()
                );
        if(feedbacks.isEmpty()){
            throw new ResourceNotFoundException("No feedbacks found!");
        }
        return feedbacks.stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FeedbackResponseDTO> getRecentFeedbacksByCourseId(int courseId, int limit) {
        User student = userService.getUserById(getCurrentUserId());
        LocalDateTime fromDate = LocalDateTime.now().minusDays(7);

        List<Feedback> feedbacks = feedbackRepo
                .getRecentFeedbacksByCourseIdAndInstitutionId(
                        courseId,
                        student.getInstitution().getInstitutionId(),
                        fromDate
                );
        return feedbacks.stream()
                .limit(limit)
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FeedbackResponseDTO> searchFeedback(Integer courseId, Integer studentId, Integer minRating,
                                                    String keyword, String studentName, Boolean anonymous,
                                                    LocalDateTime fromDate, LocalDateTime toDate) {
        int institutionId = studentId != null ? userService.getUserById(studentId).getInstitution().getInstitutionId()
                : courseId != null ? courseService.getCourseById(courseId).getInstitution().getInstitutionId()
                : 0;

        Specification<Feedback> spec = Specification.allOf(
                FeedbackSpecification.hasCourseId(courseId),
                FeedbackSpecification.hasStudentId(studentId),
                FeedbackSpecification.courseRatingGreaterThan(minRating),
                FeedbackSpecification.containsKeyword(keyword),
                FeedbackSpecification.hasStudentName(studentName),
                FeedbackSpecification.anonymousFeedbacks(anonymous),
                FeedbackSpecification.feedbackSubmittedBetween(fromDate, toDate),
                FeedbackSpecification.belongsToInstitution(institutionId)
        );

        return feedbackRepo.findAll(spec).stream()
                .map(feedbackMapper::toResponse)
                .toList();
    }

    @Override
    @Cacheable(
            value = "feedbackTrends",
            key = "'trends'"
    )
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<FeedbackTrendDTO> getFeedbackTrends(String groupBy) {
        int institutionId = SecurityUtils.getInstitutionId();

        return switch (groupBy.toUpperCase()) {
            case "DAY" -> feedbackRepo.getDailyTrendsByInstitutionId(institutionId);
            case "MONTH" -> feedbackRepo.getMonthlyTrendsByInstitutionId(institutionId);
            case "YEAR" -> feedbackRepo.getYearlyTrendsByInstitutionId(institutionId);
            default -> feedbackRepo.getMonthlyTrendsByInstitutionId(institutionId);
        };
    }

    @Override
    @Cacheable(
            value = "feedbackDistribution",
            key = "'distribution'"
    )
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<RatingDistributionDTO> getFeedbackRatings() {
        int institutionId = getStudentInstitutionId(SecurityUtils.getCurrentUserId());
        return feedbackRepo.getRatingDistribution(institutionId);
    }


    public Double avgRatingOfInstructorsLast7days(){
        LocalDateTime sevenDaysLess = LocalDateTime.now().minusDays(7);
        User currentUser = userService.getUserById(getCurrentUserId());
        return feedbackRepo.avgInstructorRatingLast7Days(currentUser.getInstitution().getInstitutionId(), sevenDaysLess);
    }

    public Double avgRatingOfCoursesLast7days(){
        LocalDateTime sevenDaysLess = LocalDateTime.now().minusDays(7);
        User currentUser = userService.getUserById(getCurrentUserId());
        return feedbackRepo.avgCourseRatingLast7Days(currentUser.getInstitution().getInstitutionId(), sevenDaysLess);
    }

    @Override
    public List<FeedbackResponseDTO> getFeedbacksByEnrollment(int enrollmentId) {
        Enrollment enrollment = enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        int institutionId = enrollment.getStudent().getInstitution().getInstitutionId();

        List<Feedback> feedbacks = feedbackRepo.findByStudent_UserIdAndCourse_CourseIdAndInstitution_InstitutionId(
                enrollment.getStudent().getUserId(),
                enrollment.getCourse().getCourseId(),
                institutionId
        );

        return feedbacks.stream()
                .map(feedbackMapper::toResponse)
                .toList();
    }

    @Override
    public Long countTotalFeedbacksByInstitution(){
        return feedbackRepo.findCountByInstitutionId(SecurityUtils.getInstitutionId());
    }
}
