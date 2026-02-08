package com.feedbacks.FeedbackSystem.service.serviceImple;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstitutionRegistrationRequest;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstitutionRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.InstitutionResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.feedbacks.FeedbackSystem.Exception.BadRequestException;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.mapper.InstitutionMapper;
import com.feedbacks.FeedbackSystem.mapper.UserMapper;
import com.feedbacks.FeedbackSystem.model.Institution;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.repository.InstitutionRepository;
import com.feedbacks.FeedbackSystem.repository.UserRepository;
import com.feedbacks.FeedbackSystem.security.SecurityUtils;
import com.feedbacks.FeedbackSystem.service.interfaces.InstitutionService;
import com.feedbacks.FeedbackSystem.service.interfaces.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionRepository institutionRepo;
    private final InstitutionMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepo;
    private final UserMapper userMapper;

    public InstitutionServiceImpl(InstitutionRepository institutionRepo, InstitutionMapper mapper, PasswordEncoder passwordEncoder, UserRepository userRepo, UserMapper userMapper) {
        this.institutionRepo = institutionRepo;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponseDTO registerInstitution(InstitutionRegistrationRequest registerRequest) {

        if (institutionRepo.existsByInstitutionCode(registerRequest.getInstitutionCode())) {
            throw new BadRequestException("Institution code already exists");
        }

        if (institutionRepo.existsByEmail(registerRequest.getInstitutionEmail())) {
            throw new BadRequestException("Institution email already exists");
        }

        Institution institution = new Institution();
        institution.setInstitutionName(registerRequest.getInstitutionName());
        institution.setInstitutionCode(registerRequest.getInstitutionCode());
        institution.setEmail(registerRequest.getInstitutionEmail());
        institution.setAddress(registerRequest.getAddress());
        institutionRepo.save(institution);

        User admin = new User();
        admin.setUsername(registerRequest.getAdminName());
        admin.setEmail(registerRequest.getAdminEmail());
        admin.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        admin.setIdentityNo(registerRequest.getIdentityNo());
        admin.setRole(User.Role.ADMIN);
        admin.setInstitution(institution);
        userRepo.save(admin);
        return userMapper.toResponse(admin);
    }

    @Override
    public InstitutionResponseDTO createInstitution(InstitutionRequestDTO dto) {
        if (institutionRepo.existsByInstitutionCode(dto.getInstitutionCode())) {
            throw new BadRequestException("Institution code already exists");
        }

        if (institutionRepo.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Institution email already exists");
        }

        Institution institution = mapper.toEntity(new Institution(), dto);
        institutionRepo.save(institution);

        log.info("event=INSTITUTION_CREATED id={} code={}",
                institution.getInstitutionId(),
                institution.getInstitutionCode());

        return mapper.toResponse(institution);
    }

    @Override
    public InstitutionResponseDTO getInstitutionById(int institutionId) {
        Institution institution = institutionRepo.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));
        return mapper.toResponse(institution);
    }

    @Override
    public List<InstitutionResponseDTO> getAllInstitutions() {
        return institutionRepo.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public InstitutionResponseDTO updateInstitution(InstitutionRequestDTO dto) {
        Institution institution = institutionRepo.findByAdminId(SecurityUtils.getCurrentUserId());
        if (institution == null ){
            throw new ResourceNotFoundException("Institution not found");
        }

        mapper.toEntity(institution, dto);
        institutionRepo.save(institution);
        return mapper.toResponse(institution);
    }

    @Override
    public void deleteInstitution() {
        int institutionId = SecurityUtils.getInstitutionId();
        if (!institutionRepo.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution not found");
        }
        institutionRepo.deleteById(institutionId);
    }

    @Override
    public List<InstitutionResponseDTO> searchInstitutions(String name) {
        return institutionRepo.searchByName(name)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public Long totalUsers() {
        return institutionRepo.totalUsersByInstitution(SecurityUtils.getInstitutionId());
    }

    @Override
    public Long totalCourses() {
        return institutionRepo.totalCoursesByInstitution(SecurityUtils.getInstitutionId());
    }
}
