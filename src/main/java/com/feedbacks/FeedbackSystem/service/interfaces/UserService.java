package com.feedbacks.FeedbackSystem.service.interfaces;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.UserRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedStudentsDTO;
import com.feedbacks.FeedbackSystem.model.User;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();

    User getUserById(int userId);

    UserResponseDTO getUserResponseById(int userId);

    List<UserResponseDTO> getAllUsersAsResponseDTO();

    List<User> getAllUsersById();

    UserResponseDTO addUser(UserRequestDTO userRequestDTO, Integer institutionId);

    UserResponseDTO updateUser(int userId, UserRequestDTO userRequestDTO);

    void deleteByUserId(int userId);

    UserResponseDTO getByRollNo(String rollNo);

    UserResponseDTO getByEmail(String email);

    List<UserResponseDTO> getAllUsersByRole(User.Role roleEnum);

    List<UserResponseDTO> getUsersByInstitution(User.Role role);

    Integer getTotalStudentsCount();

    List<TopRatedStudentsDTO> getTopRatedStudents(int pageNumber, int limit);

    List<UserResponseDTO> searchUser(Integer userId,
                                     Integer studentId,
                                     String studentName,
                                     String rollNo);

    List<UserResponseDTO> searchAdmin(Integer adminUId,
                                      String adminId);


    // Single method to return any of the below methods
    List<UserResponseDTO> getStudents(Boolean hasFeedbacks,
                                      Boolean enrolled,
                                      Integer courseId);

    List<UserResponseDTO> findStudentsWithoutFeedback();

    List<UserResponseDTO> findStudentsWithoutEnrollment();

    List<UserResponseDTO> studentsEnrolledToThisCourse(Integer courseId);

    Integer countStudentsByInstitution();
}
