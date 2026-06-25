package com.feedbacks.FeedbackSystem.service.serviceImple;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstructorRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.InstructorResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbacksByInstructor;
import com.feedbacks.FeedbackSystem.DTO.analytics.InstructorRankingDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedInstructorsDTO;
import com.feedbacks.FeedbackSystem.Exception.BadRequestException;
import com.feedbacks.FeedbackSystem.Exception.NotAllowedException;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.mapper.InstructorMapper;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Institution;
import com.feedbacks.FeedbackSystem.model.Instructor;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.repository.CourseRepository;
import com.feedbacks.FeedbackSystem.repository.InstitutionRepository;
import com.feedbacks.FeedbackSystem.repository.InstructorRepository;
import com.feedbacks.FeedbackSystem.repository.UserRepository;
import com.feedbacks.FeedbackSystem.security.SecurityUtils;
import com.feedbacks.FeedbackSystem.service.interfaces.InstructorService;
import com.feedbacks.FeedbackSystem.specification.InstructorSpecification;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class InstructorServiceImpl implements InstructorService {

    private final InstructorRepository instructorRepo;
    private final CourseRepository courseRepo;
    private final InstitutionRepository institutionRepo;
    private final InstructorMapper instructorMapper;
    private final UserRepository userRepo;

    public InstructorServiceImpl(InstructorRepository instructorRepo,
                                 CourseRepository courseRepo,
                                 InstitutionRepository institutionRepo,
                                 InstructorMapper instructorMapper, UserRepository userRepo) {
        this.instructorRepo = instructorRepo;
        this.courseRepo = courseRepo;
        this.institutionRepo = institutionRepo;
        this.instructorMapper = instructorMapper;
        this.userRepo = userRepo;
    }

    @Override
    public List<Instructor> getAllInstructor() {
        return instructorRepo.findAll();
    }

    @Override
    public List<InstructorResponseDTO> getAllInstructorsAsResponse() {
        return getAllInstructor()
                        .stream()
                        .map(instructorMapper::toResponse)
                        .toList();
    }

    @Override
    public List<InstructorResponseDTO> getAllInstructorsByInstitution(){
        List<Instructor> instructorList = instructorRepo
                .findAllInstructorByInstitution(SecurityUtils.getInstitutionId());
        return instructorList.stream()
                .map(instructor -> instructorMapper.toResponse(instructor))
                .toList();
    }

    @Override
    public Instructor getInstructorById(Integer instructorId) {
        return instructorRepo.findByInstructorId(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Instructor not found with Id: " + instructorId));
    }

    @Override
    public InstructorResponseDTO getInstructorResponseById(int instructorId) {
        Instructor instructor = getInstructorById(instructorId);
        return instructorMapper.toResponse(instructor);
    }

    @Transactional
    @Override
    public void updateInstructorStateOnFeedbackAdd(Instructor instructor, int rating, boolean isEditing){
        long count = instructor.getFeedbackCount();
        if (!isEditing) count++;
        double avgRating = ((instructor.getAvgRating() * instructor.getFeedbackCount()) + rating) / count;
        instructor.setAvgRating(avgRating);
        instructor.setFeedbackCount(count);
    }

    @Transactional
    @Override
    public void updateInstructorStateOnFeedbackRemove(Instructor instructor, int rating){
        long count = instructor.getFeedbackCount() - 1;
        if (count == 0){
            instructor.setAvgRating(0.0);
            instructor.setFeedbackCount(0L);
            return;
        }
        double avgRating = ((instructor.getAvgRating() * count + 1) - rating) / count;
        instructor.setAvgRating(avgRating);
        instructor.setFeedbackCount(count);
    }

    @Override
    public InstructorResponseDTO addInstructor(InstructorRequestDTO dto) {

        int institutionId = SecurityUtils.getInstitutionId();

        Institution institution = institutionRepo.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        instructorMapper.toUser(dto, user, institution, true);
        userRepo.save(user);

        Instructor instructor = instructorMapper.toEntity(user);
        instructorRepo.save(instructor);

        log.info("event=INSTRUCTOR_CREATED instructorId={} institutionId={}",
                instructor.getInstructorId(), institutionId);

        return instructorMapper.toResponse(instructor);
    }

    @Override
    public InstructorResponseDTO updateInstructorById(int instructorId, InstructorRequestDTO dto) {
        int institutionId = SecurityUtils.getInstitutionId();
        Instructor instructor = getInstructorById(instructorId);

        if (instructor.getUser().getInstitution().getInstitutionId() != institutionId) {
            throw new BadRequestException("Instructor does not belong to this institution");
        }

        if (userRepo.existsByEmailAndUserIdNot(dto.getEmail(), instructor.getUser().getUserId())) {
            throw new BadRequestException("Email already exists");
        }

        instructorMapper.toUser(dto, instructor.getUser(), instructor.getUser().getInstitution(), false);
        instructorRepo.save(instructor);

        log.info("event=INSTRUCTOR_UPDATED instructorId={} institutionId={}",
                instructorId, institutionId);

        return instructorMapper.toResponse(instructor);
    }

    @Override
    public void deleteInstructorById(int instructorId) {
        Instructor instructor = getInstructorById(instructorId);
        instructor.setDeletedAt(LocalDateTime.now());
        instructor.setDeletedBy(SecurityUtils.getCurrentUsername());
        instructor.setDeleted(true);
        instructor.getCourses().forEach(course -> course.setInstructor(null));

        log.info("event=INSTRUCTOR_DELETED instructorId={} deletedBy={}",
                instructorId, instructor.getDeletedBy());

        instructorRepo.delete(instructor);
    }

    @Override
    public InstructorResponseDTO assignCourseToInstructor(int instructorId, int courseId) {
        Instructor instructor = getInstructorById(instructorId);
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found."));

        if (course.getInstructor() != null) {
            throw new BadRequestException("Course already assigned to an instructor: "
                    + course.getInstructor().getUser().getUsername());
        }

        if (instructor.getUser().getInstitution() != null && course.getInstitution() != null
                && !instructor.getUser().getInstitution().getInstitutionId()
                .equals(course.getInstitution().getInstitutionId())) {
            throw new BadRequestException("Instructor and Course belong to different institutions");
        }

        instructor.addCourse(course);
        log.info("event=COURSE_ASSIGNED courseId={} instructorId={} assignedAt={}",
                courseId, instructorId, LocalDateTime.now());

        return instructorMapper.toResponse(instructorRepo.save(instructor));
    }

    @Override
    public void unassignCourseFromInstructor(int instructorId, int courseId) {
        Instructor instructor = getInstructorById(instructorId);
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getInstructor() == null) {
            throw new NotAllowedException("No instructor assigned to this course");
        }

        if (course.getInstructor().getInstructorId() != instructorId) {
            throw new NotAllowedException("This course is not assigned to this instructor");
        }

        instructor.removeCourse(course);
        courseRepo.save(course);

        log.info("event=COURSE_UNASSIGNED courseId={} instructorId={} assignedAt={}",
                courseId, instructorId, LocalDateTime.now());
    }


    @Transactional
    public void unassignInstructorFromInstitution(int instructorId) {
        Instructor instructor = getInstructorById(instructorId);

        instructor.getCourses()
                .forEach(course -> course.setInstructor(null));

        instructor.setDeleted(true);
        instructor.setDeletedAt(LocalDateTime.now());
        instructor.setDeletedBy(SecurityUtils.getCurrentUsername());

        User user = getUser(instructor);
        user.setRole(User.Role.STUDENT);
        instructorRepo.save(instructor);
    }

    private static User getUser(Instructor instructor) {
        return instructor.getUser();
    }


    @Override
    public List<InstructorResponseDTO> findAllSoftDeletedInstructors(){
        List<Instructor> deleted = instructorRepo.findAllDeletedInstructor();
        return deleted.stream().map(instructorMapper::toResponse).toList();
    }

    @Override
    public InstructorResponseDTO restoreInstructor(int instructorId){
        Instructor instructor = getInstructorById(instructorId);
        instructor.setDeleted(false);
        instructor.setDeletedAt(null);
        instructor.setRestoredBy(SecurityContextHolder.getContext().getAuthentication().getName());
        instructorRepo.save(instructor);
        return instructorMapper.toResponse(instructor);
    }

    @Transactional
    @Override
    public void deleteInstructorPermanently(int instructorId){
        Instructor instructor = getInstructorById(instructorId);
        if (instructor == null){
            throw new BadRequestException("No instructor found for ID " + instructorId);
        }
        int userId = instructor.getUser().getUserId();
        instructorRepo.deletePermanently(instructorId);
        userRepo.deleteUserById(userId);
    }

    @Override
    public List<InstructorResponseDTO> getUnassignedInstructors(){
        List<Instructor> unassigned = instructorRepo.findUnassignedInstructors(SecurityUtils.getInstitutionId());
        if (unassigned.isEmpty()) throw new ResourceNotFoundException("Every instructor is assigned to courses");
        return unassigned.stream().map(instructorMapper::toResponse).toList();
    }

    @Override
    public List<String> viewAssignedCourseForInstructor(int instructorId){
        Instructor instructor = getInstructorById(instructorId);
        if (instructor.getCourses().isEmpty())
            throw new ResourceNotFoundException("No courses assigned to this instructor");
        return instructor.getCourses().stream()
                .map(Course::getCourseName)
                .toList();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<FeedbacksByInstructor> getAllFeedbacksByInstructor(){
        return instructorRepo.getAllFeedbacksToInstructor();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<TopRatedInstructorsDTO> getAllTopRatedInstructors() {
        return instructorRepo.findTopRatedInstructor();
    }

    @Override
    public List<InstructorResponseDTO> searchInstructor(Integer instructorId,
                                                        String instructorName,
                                                        String courseName){
        Specification<Instructor> spec = Specification.allOf(
                InstructorSpecification.hasInstructorId(instructorId),
                InstructorSpecification.hasInstructorName(instructorName),
                InstructorSpecification.byAssignedCourseName(courseName)
        );
        return instructorRepo.findAll(spec).stream()
                .map(instructorMapper::toResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "TopInstructors", key = "#page + ':' + #size")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<InstructorRankingDTO> getTopRatedInstructor(int page, int size){
        return instructorRepo.getTopRatedInstructor(SecurityUtils.getInstitutionId(), PageRequest.of(page, size));
    }

    @Override
    public Long getTotalInstructorCountByInstitution() {
        return instructorRepo.findCountByInstitutionId(SecurityUtils.getInstitutionId());
    }

}

