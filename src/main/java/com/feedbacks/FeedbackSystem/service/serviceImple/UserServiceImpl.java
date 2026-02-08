package com.feedbacks.FeedbackSystem.service.serviceImple;

import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedStudentsDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.UserRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.feedbacks.FeedbackSystem.Exception.BadRequestException;
import com.feedbacks.FeedbackSystem.Exception.NotAllowedException;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.mapper.UserMapper;
import com.feedbacks.FeedbackSystem.model.Institution;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.repository.InstitutionRepository;
import com.feedbacks.FeedbackSystem.repository.UserRepository;
import com.feedbacks.FeedbackSystem.security.SecurityUtils;
import com.feedbacks.FeedbackSystem.service.interfaces.UserService;
import com.feedbacks.FeedbackSystem.specification.UserSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final InstitutionRepository institutionRepo;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepo,
                           InstitutionRepository institutionRepo,
                           UserMapper userMapper) {
        this.userRepo = userRepo;
        this.institutionRepo = institutionRepo;
        this.userMapper = userMapper;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public User getUserById(int userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    @Override
    public UserResponseDTO getUserResponseById(int userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsersAsResponseDTO() {
        return getAllUsers().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public List<User> getAllUsersById() {
        return userRepo.findAllById(
                getAllUsers().stream()
                        .map(User::getUserId)
                        .toList()
        );
    }

    public Page<UserResponseDTO> getAllUserInInstitution(int pageNumber, int pageSize){
        int institutionId = SecurityUtils.getInstitutionId();
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return userRepo.findAllUserInInstitution(institutionId, pageable)
                .map(userMapper::toResponse);
    }

    // ADD User
    @Override
    public UserResponseDTO addUser(UserRequestDTO userRequestDTO, Integer institutionId) {
        Institution institution = institutionRepo.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

        Optional<User> isExist = userRepo.findByEmail(userRequestDTO.getEmail());
        if (isExist.isPresent()) {
            throw new BadRequestException("This email already exists.");
        }

        User user = new User();
        user = userMapper.toEntity(institutionId, user, userRequestDTO);
        user.setInstitution(institution);

        userRepo.save(user);

        log.info(
                "event=NEW_USER_REGISTERED userName={} userIdentity={} institutionId={}  userRole={} registeredAt={}",
                userRequestDTO.getUsername(),
                userRequestDTO.getIdentityNo(),
                institutionId,
                userRequestDTO.getRole(),
                LocalDateTime.now()
        );

        return userMapper.toResponse(user);
    }

    // update user
    @Override
    public UserResponseDTO updateUser(int userId, UserRequestDTO userRequestDTO) {
        int institutionId = SecurityUtils.getInstitutionId();
        User userExist = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found! Id: " + userId));

        if (userExist.getInstitution().getInstitutionId() != institutionId) {
            throw new BadRequestException("User does not belong to this institution");
        }

        if (userRepo.existsByEmailAndUserIdNot(userRequestDTO.getEmail(), userId)) {
            throw new BadRequestException("Email already exists.");
        }

        userExist = userMapper.toEntity(institutionId, userExist, userRequestDTO);
        userRepo.save(userExist);

        log.info(
                "event=USER_UPDATED userId={} userName={} institutionId={}",
                userExist.getUserId(),
                userExist.getUsername(),
                institutionId
        );
        return userMapper.toResponse(userExist);
    }


    @Override
    public void deleteByUserId(int userId) {
        int institutionId = SecurityUtils.getInstitutionId();
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not exist - ID:" + userId));

        if (user.getInstitution().getInstitutionId() != institutionId) {
            throw new BadRequestException("User does not belong to this institution");
        }

        userRepo.deleteById(userId);
        log.info("event=USER_DELETED userId={} institutionId={}", userId, institutionId);
    }


    @Override
    public UserResponseDTO getByRollNo(String rollNo) {
        int institutionId = SecurityUtils.getInstitutionId();
        User user = userRepo.findByIdentityNoAndInstitution_InstitutionId(rollNo, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Student with rollNo " + rollNo + " not found."));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponseDTO getByEmail(String email) {
        User student = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student with email " + email + " not found."));

        return userMapper.toResponse(student);
    }


    @Override
    public List<UserResponseDTO> getAllUsersByRole(User.Role roleEnum) {
        int institutionId = SecurityUtils.getInstitutionId();
        return userRepo.findByInstitutionAndRole(institutionId, roleEnum)
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getTotalStudentsCount() {
        return userRepo.totalStudentsCount();
    }


    @Override
    public List<TopRatedStudentsDTO> getTopRatedStudents(int pageNumber, int limit) {
        int institutionId = SecurityUtils.getInstitutionId();
        Pageable pageable = PageRequest.of(pageNumber, limit);
        return userRepo.findTopStudentsByFeedbacks(institutionId, pageable);
    }


    @Override
    public List<UserResponseDTO> searchUser(Integer userId,
                                            Integer studentId,
                                            String studentName,
                                            String rollNo) {
        int institutionId = SecurityUtils.getInstitutionId();
        Specification<User> specification = Specification.allOf(
                UserSpecification.belongsToInstitution(institutionId),
                UserSpecification.hasUserId(userId),
                UserSpecification.studentById(studentId),
                UserSpecification.hasStudentName(studentName),
                UserSpecification.hasRollNo(rollNo)
        );

        return userRepo.findAll(specification)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }


    @Override
    public List<UserResponseDTO> searchAdmin(Integer adminUId,
                                             String adminId) {
        int institutionId = SecurityUtils.getInstitutionId();
        Specification<User> specification = Specification.allOf(
                UserSpecification.belongsToInstitution(institutionId),
                UserSpecification.adminById(adminUId),
                UserSpecification.hasAdminIdNo(adminId)
        );

        return userRepo.findAll(specification)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }


    @Override
    public List<UserResponseDTO> getStudents(Boolean hasFeedbacks,
                                             Boolean enrolled,
                                             Integer courseId) {
        int institutionId = SecurityUtils.getInstitutionId();
        if (Boolean.TRUE.equals(hasFeedbacks)) {
            return findStudentsWithoutFeedback();
        } else if (Boolean.TRUE.equals(enrolled)) {
            return findStudentsWithoutEnrollment();
        } else {
            return studentsEnrolledToThisCourse(courseId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> findStudentsWithoutFeedback() {
        int institutionId = SecurityUtils.getInstitutionId();
        Specification<User> specification = Specification.allOf(
                UserSpecification.belongsToInstitution(institutionId),
                UserSpecification.studentsWithoutFeedback(institutionId)
        );

        return userRepo.findAll(specification)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public List<UserResponseDTO> findStudentsWithoutEnrollment() {
        int institutionId = SecurityUtils.getInstitutionId();
        Specification<User> specification = Specification.allOf(
                UserSpecification.belongsToInstitution(institutionId),
                UserSpecification.studentsWithoutEnrollments(institutionId)
        );

        return userRepo.findAll(specification)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public List<UserResponseDTO> studentsEnrolledToThisCourse(Integer courseId) {
        int institutionId = SecurityUtils.getInstitutionId();
        Specification<User> specification = Specification.allOf(
                UserSpecification.belongsToInstitution(institutionId),
                UserSpecification.studentsEnrolledToThisCourse(courseId, institutionId)
        );

        return userRepo.findAll(specification)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public List<UserResponseDTO> getUsersByInstitution(User.Role role) {
        int institutionId = SecurityUtils.getInstitutionId();
        System.out.println(userRepo.findByInstitutionAndRole(institutionId, role)
                .stream()
                .map(userMapper::toResponse)
                .toList());
        return userRepo.findByInstitutionAndRole(institutionId, role)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public Integer countStudentsByInstitution() {
        int institutionId = SecurityUtils.getInstitutionId();
        return userRepo.countStudentsByInstitution(institutionId);
    }
}

