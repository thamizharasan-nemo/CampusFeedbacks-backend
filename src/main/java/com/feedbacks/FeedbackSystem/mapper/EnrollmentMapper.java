package com.feedbacks.FeedbackSystem.mapper;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.EnrollmentRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.EnrollmentResponseDTO;
import com.feedbacks.FeedbackSystem.Exception.BadRequestException;
import com.feedbacks.FeedbackSystem.Exception.NotAllowedException;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Enrollment;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.repository.EnrollmentRepository;
import com.feedbacks.FeedbackSystem.service.serviceImple.CourseServiceImpl;
import com.feedbacks.FeedbackSystem.service.serviceImple.UserServiceImpl;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    private final UserServiceImpl userService;
    private final CourseServiceImpl courseService;
    private final EnrollmentRepository enrollmentRepo;

    public EnrollmentMapper(UserServiceImpl userService,
                            CourseServiceImpl courseService,
                            EnrollmentRepository enrollmentRepo) {
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentRepo = enrollmentRepo;
    }

    public Enrollment toEntity(EnrollmentRequestDTO requestDTO, Enrollment enrollment){

        User student = userService.getUserById(requestDTO.getStudentId());
        Course course = courseService.getCourseById(requestDTO.getCourseId());

        if (!student.getRole().name().equals("STUDENT")) {
            throw new NotAllowedException("Only students can enroll in courses.");
        }

        if (!student.getInstitution().getInstitutionId()
                .equals(course.getInstitution().getInstitutionId())) {
            throw new BadRequestException(
                    "Student and Course belong to different institutions"
            );
        }

        int institutionId = student.getInstitution().getInstitutionId();

        int enrollmentCount =
                enrollmentRepo.countStudentTotalEnrollmentsByInstitution(
                        student.getUserId(), institutionId
                );

        if (enrollmentCount >= 10) {
            throw new NotAllowedException(
                    "A student can enroll in a maximum of 10 courses per institution."
            );
        }

        if (enrollmentRepo.existsByCourse_CourseIdAndStudent_UserIdAndInstitution_InstitutionId(
                course.getCourseId(),
                student.getUserId(),
                institutionId
        )) {
            throw new BadRequestException(
                    "Student with roll no " + student.getIdentityNo()
                            + " is already enrolled in this course"
            );
        }
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setInstitution(student.getInstitution());

        return enrollment;
    }

    public EnrollmentResponseDTO toResponse(Enrollment enrollment){

        EnrollmentResponseDTO response = new EnrollmentResponseDTO();

        response.setEnrollmentId(enrollment.getEnrollId());
        response.setInstitutionId(enrollment.getInstitution().getInstitutionId());
        response.setInstitutionName(enrollment.getInstitution().getInstitutionName());
        response.setStudentName(enrollment.getStudent().getUsername());
        response.setStudentRollNo(enrollment.getStudent().getIdentityNo());
        response.setCourseName(enrollment.getCourse().getCourseName());
        response.setInstructorName(
                enrollment.getCourse().getInstructor() != null
                        ? enrollment.getCourse().getInstructor().getUser().getUsername()
                        : "Not Assigned"
        );
        response.setEnrolledDate(enrollment.getEnrollmentDate());

        return response;
    }
}
