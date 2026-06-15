package com.finance.app.user;

import com.finance.app.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getProfile(UUID userId) {
        User user = findUser(userId);
        return UserResponse.from(user);
    }

    public UserResponse updateProfile(UUID userId, UpdateUserRequest request) {
        User user = findUser(userId);

        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.defaultCurrency() != null) {
            user.setDefaultCurrency(request.defaultCurrency());
        }

        user = userRepository.save(user);
        return UserResponse.from(user);
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}
