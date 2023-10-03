package com.nmontiel.superheroes.user.service;

import com.nmontiel.superheroes.exception.BadRequestException;
import com.nmontiel.superheroes.exception.NotFoundException;
import com.nmontiel.superheroes.user.data.UserDto;
import com.nmontiel.superheroes.user.entity.UserEntity;
import com.nmontiel.superheroes.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.management.BadAttributeValueExpException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class UserService {
    private final ModelMapper mapper;
    private final UserRepository repo;

    private UserDto convertToDto(UserEntity entity){
        return mapper.map(entity, UserDto.class);
    }
    private UserEntity convertToEntity(UserDto dto){
        return mapper.map(dto, UserEntity.class);
    }

    public List<UserDto> findAllUsers(){
        var userEntityList = new ArrayList<>(repo.findAll());
        return userEntityList.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public UserDto findUserById(final UUID id){
        var user = repo.findById(id).orElseThrow(() -> new NotFoundException("User by id " + id + " was not found"));
        return convertToDto(user);
    }

    private byte[] createSalt(){
        var random = new SecureRandom();
        var salt = new byte[128];
        random.nextBytes(salt);
        return salt;
    }

    private byte[] createPasswordHash(String password, byte[] salt) throws NoSuchAlgorithmException {
        var md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        return md.digest(password.getBytes(StandardCharsets.UTF_8));
    }

    public UserDto createUser(UserDto userDto, String password) throws NoSuchAlgorithmException {
        var user = convertToEntity(userDto);
        if (password.isBlank()) throw new IllegalArgumentException("Password is required");

        var existsEmail = repo.selectExistsEmail(user.getEmail());
        if (existsEmail) throw new BadRequestException("Email " + user.getEmail() + " taken");
        byte[] salt = createSalt();
        byte[] hashedPass = createPasswordHash(password, salt);
        user.setStoredSalt(salt);
        user.setStoredHash(hashedPass);
        repo.save(user);
        return convertToDto(user);
    }

    public void updateUser(UUID id, UserDto userDto, String password) throws NoSuchAlgorithmException {
        var user = findOrThrow(id);
        var userParam = convertToEntity(userDto);

        user.setEmail(userParam.getEmail());
        user.setMobileNumber(userParam.getMobileNumber());

        if (!password.isBlank()){
            byte[] salt = createSalt();
            byte[] hashedPass = createPasswordHash(password, salt);
            user.setStoredSalt(salt);
            user.setStoredHash(hashedPass);
        }
        repo.save(user);
    }
    public void removeUserById(UUID id) {
        findOrThrow(id);
        repo.deleteById(id);
    }
    private UserEntity findOrThrow(final UUID id) {
        return repo
            .findById(id)
            .orElseThrow(
                () -> new NotFoundException("User by id " + id + " was not found")
            );
    }
}
