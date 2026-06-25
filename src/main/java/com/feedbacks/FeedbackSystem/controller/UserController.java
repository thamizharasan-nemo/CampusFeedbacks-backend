package com.feedbacks.FeedbackSystem.controller;


import com.feedbacks.FeedbackSystem.DTO.ApiResponse;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.UserRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedStudentsDTO;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.service.interfaces.UserService;
import com.feedbacks.FeedbackSystem.service.serviceImple.UserServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    public void started() {
        log.info("🔥 VERSION CHECK: 2026-01-13 build-2 (Institution Enabled)");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "All users fetched successfully",
                        userService.getAllUsers()
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/admin/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable int userId) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "User fetched successfully",
                        userService.getUserById(userId)
                )
        );
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsersAsResponseDTO() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "Fetched all users",
                        userService.getAllUsersAsResponseDTO()
                )
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserResponseById(@PathVariable int userId) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "User details fetched",
                        userService.getUserResponseById(userId)
                )
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsersByRole(@RequestParam(defaultValue = "STUDENT") String role) {
        User.Role roleEnum = User.Role.valueOf(role.toUpperCase());
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "Users fetched by role",
                        userService.getAllUsersByRole(roleEnum)
                )
        );
    }

    @PreAuthorize("permitAll()")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> addUser(@Valid @RequestBody UserRequestDTO userRequestDTO,
                                                                @RequestParam String institutionCode
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "User created successfully",
                        userService.addUser(userRequestDTO, institutionCode)
                )
        );
    }

    @PreAuthorize("permitAll()")
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(@PathVariable int userId,
                                                                   @Valid @RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "User updated successfully",
                        userService.updateUser(userId, userRequestDTO)
                )
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable int userId) {
        userService.deleteByUserId(userId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "User deleted successfully", null)
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/rollno")
    public ResponseEntity<ApiResponse<UserResponseDTO>> findByRollNo(@RequestParam String rollNo) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "User fetched by roll number",
                        userService.getByRollNo(rollNo)
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/email")
    public ResponseEntity<ApiResponse<UserResponseDTO>> findByEmail(@RequestParam String email) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "User fetched by email",
                        userService.getByEmail(email)
                )
        );
    }


    @GetMapping("/students/count")
    public ResponseEntity<ApiResponse<Integer>> countTotalStudents() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "Total students count fetched",
                        userService.getTotalStudentsCount()
                )
        );
    }

    @GetMapping("/students/top")
    public ResponseEntity<ApiResponse<List<TopRatedStudentsDTO>>> topRatedStudents(@RequestParam(defaultValue = "0") int pageNumber,
                                                                                   @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "Top rated students fetched",
                        userService.getTopRatedStudents(pageNumber, limit)
                )
        );
    }


    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getUsersByInstitution(@RequestParam User.Role role) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "Users fetched by institution",
                        userService.getUsersByInstitution(role)
                )
        );
    }

    @GetMapping("/institution/students/count")
    public ResponseEntity<ApiResponse<Integer>> countStudents() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "Institution student count fetched",
                        userService.countStudentsByInstitution()
                )
        );
    }


    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> searchUser(@RequestParam(required = false) Integer userId,
                                                                         @RequestParam(required = false) Integer studentId,
                                                                         @RequestParam(required = false) String studentName,
                                                                         @RequestParam(required = false) String rollNo
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "User search results fetched",
                        userService.searchUser(userId,
                                studentId, studentName, rollNo
                        )
                )
        );
    }

    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> searchAdmin(@RequestParam(required = false) Integer adminUId,
                                                                          @RequestParam(required = false) String adminId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "Admin search results fetched",
                        userService.searchAdmin(adminUId, adminId)
                )
        );
    }


    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getStudents(
            @RequestParam(required = false, defaultValue = "false") Boolean hasFeedback,
            @RequestParam(required = false, defaultValue = "false") Boolean enrolled,
            @RequestParam(required = false) Integer courseId
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Students fetched successfully",
                        userService.getStudents(hasFeedback, enrolled,
                                courseId
                        )
                )
        );
    }

    @GetMapping("/feedbacks/students")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getStudentsWithoutFeedbacks() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Students without feedback fetched",
                        userService.findStudentsWithoutFeedback()
                )
        );
    }

    @GetMapping("/unrolled/students")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getStudentsWithoutEnrollment() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Students without enrollment fetched",
                        userService.findStudentsWithoutEnrollment()
                )
        );
    }

    @GetMapping("/students/course")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getStudentsEnrolledToThisCourse(@RequestParam Integer courseId) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Students enrolled to course fetched",
                        userService.studentsEnrolledToThisCourse(courseId)
                )
        );
    }



}

