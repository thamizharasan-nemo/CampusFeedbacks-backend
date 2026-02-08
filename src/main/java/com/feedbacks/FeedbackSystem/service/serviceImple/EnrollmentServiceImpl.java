package com.feedbacks.FeedbackSystem.service.serviceImple;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.EnrollmentRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.CourseResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.EnrollmentResponseDTO;
import com.feedbacks.FeedbackSystem.Exception.BadRequestException;
import com.feedbacks.FeedbackSystem.Exception.NotAllowedException;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.mapper.CourseMapper;
import com.feedbacks.FeedbackSystem.mapper.EnrollmentMapper;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Enrollment;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.repository.EnrollmentRepository;
import com.feedbacks.FeedbackSystem.security.SecurityUtils;
import com.feedbacks.FeedbackSystem.service.interfaces.EnrollmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepo;
    private final UserServiceImpl userService;
    private final CourseServiceImpl courseService;
    private final EnrollmentMapper enrollmentMapper;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepo, UserServiceImpl userService, CourseServiceImpl courseService, EnrollmentMapper enrollmentMapper) {
        this.enrollmentRepo = enrollmentRepo;
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentMapper = enrollmentMapper;
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepo.findAll();
    }

    public Enrollment getEnrollmentById(int enrollmentId) {
        return enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("No enrollment found."));
    }

    @Override
    public Page<EnrollmentResponseDTO> getAllEnrollmentsToInstitutionAsPage(int page, int size){
        int institutionId = SecurityUtils.getInstitutionId();
        Pageable pageable = PageRequest.of(page, size);
        return enrollmentRepo.findByInstitutionId(institutionId, pageable)
                .map(enrollmentMapper::toResponse);
    }

    @Override
    public EnrollmentResponseDTO getEnrollmentResponseById(int enrollmentId) {
        Enrollment enrollment = enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("No enrollment found."));
        return enrollmentMapper.toResponse(enrollment);
    }

    @Override
    public List<EnrollmentResponseDTO> getAllEnrollmentsByStudentId() {
        int studentId = SecurityUtils.getCurrentUserId();
        User student = userService.getUserById(studentId);
        List<Enrollment> enrollments = enrollmentRepo.findByStudent(student);
        return enrollments.stream()
                .map(enrollmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentResponseDTO> getAllEnrollmentsByRollNo(String rollNo) {

        Integer institutionId = SecurityUtils.getInstitutionId();
        List<Enrollment> enrollments = enrollmentRepo.findByStudent_IdentityNoAndInstitution_InstitutionId(
                rollNo,
                institutionId
        );
        if(enrollments.isEmpty()){
            throw new ResourceNotFoundException("No enrollments found for roll number "+rollNo);
        }
        return enrollments.stream()
                .map(enrollmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentResponseDTO> getEnrollmentsToInstitution(){
        return enrollmentRepo.findByInstitution_InstitutionId(SecurityUtils.getInstitutionId())
                .stream()
                .map(enrollmentMapper::toResponse)
                .toList();
    }

    @Override
    public List<CourseResponseDTO> getStudentEnrolledCourse() {
        User student = userService.getUserById(SecurityUtils.getCurrentUserId());

        if (student.getRole() != User.Role.STUDENT){
            throw new BadRequestException("You're not a STUDENT, only students can get courses");
        }

        List<Course> courses = enrollmentRepo.findCoursesByStudentId(student.getUserId());

        return courses.stream()
                .map(courseService::convertToResponse)
                .toList();
    }

    @Override
    public Boolean isEnrolled(int courseId){
        int studentId = SecurityUtils.getCurrentUserId();
        int institutionId = SecurityUtils.getInstitutionId();
        return enrollmentRepo.existsByCourse_CourseIdAndStudent_UserIdAndInstitution_InstitutionId(
                courseId,
                studentId,
                institutionId
        );
    }

    @Override
    public EnrollmentResponseDTO enrollToCourse(EnrollmentRequestDTO requestDTO){
        Enrollment enrollment = new Enrollment();
        requestDTO.setStudentId(SecurityUtils.getCurrentUserId());
        enrollment = enrollmentMapper.toEntity(requestDTO, enrollment);
        if (enrollment == null) {
            throw new ResourceNotFoundException("Enrollment failed");
        }
        enrollmentRepo.save(enrollment);

        log.info(
                "event=ENROLLED_TO_COURSE studentId={} courseId={} enrolledAt={}",
                enrollment.getStudent().getIdentityNo(),
                enrollment.getCourse().getCourseId(),
                enrollment.getEnrollmentDate()
        );

        return enrollmentMapper.toResponse(enrollment);
    }

    @Override
    public void unrollToCourse(int courseId){
        int studentId = SecurityUtils.getCurrentUserId();
        User student = userService.getUserById(studentId);
        Course course = courseService.getCourseById(courseId);
        int institutionId = SecurityUtils.getInstitutionId();

        if(!enrollmentRepo.existsByCourse_CourseIdAndStudent_UserIdAndInstitution_InstitutionId(
                courseId,
                studentId,
                institutionId
        )){
            throw new NotAllowedException("The student with roll number "+student.getIdentityNo()+" hasn't enrolled to this course "+course.getCourseName());
        }

        Enrollment enrollment = enrollmentRepo
                .findByStudent_UserIdAndCourse_CourseIdAndInstitution_InstitutionId(
                        studentId,
                        courseId,
                        institutionId
                );

        enrollmentRepo.delete(enrollment);

        log.info(
                "event=UNROLLED_TO_COURSE studentId={} courseId={} enrolledAt={}",
                enrollment.getStudent().getIdentityNo(),
                enrollment.getCourse().getCourseId(),
                enrollment.getEnrollmentDate()
        );
    }


    @Override
    public EnrollmentResponseDTO findEnrollmentByStudentIdAndCourseId(EnrollmentRequestDTO requestDTO){
        Integer institutionId = SecurityUtils.getInstitutionId();
        Enrollment enrollment = enrollmentRepo
                .findByStudent_UserIdAndCourse_CourseIdAndInstitution_InstitutionId(
                        requestDTO.getStudentId(),
                        requestDTO.getCourseId(),
                        institutionId
                        );
        if(enrollment == null){
            throw new ResourceNotFoundException("No enrollment found for student to this course.");
        }
        return enrollmentMapper.toResponse(enrollment);
    }


    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public int getCourseEnrollmentCount(int courseId){
        int institutionId = SecurityUtils.getInstitutionId();
        return enrollmentRepo.getCourseEnrollmentCount(courseId, institutionId);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public int getStudentEnrollmentCount(int studentId) {
        Integer institutionId = SecurityUtils.getInstitutionId();
        return enrollmentRepo.getCourseEnrollmentCount(studentId, institutionId);
    }


    @Override
    public long countEnrollsByInstitutionId(){
        return enrollmentRepo.countByInstitution_InstitutionId(SecurityUtils.getInstitutionId());
    }

}
