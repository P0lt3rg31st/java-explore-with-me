package ru.practicum.ewm.main.server.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.handler.exceptions.BadRequestException;
import ru.practicum.ewm.dto.handler.exceptions.ConflictException;
import ru.practicum.ewm.dto.handler.exceptions.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User create(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Integrity constraint has been violated.");
        }
    }

    public List<User> getUsers(List<Long> ids, int from, int size) {
        validatePagination(from, size);

        if (ids == null) {
            return userRepository.findAllWithOffset(from, size);
        }
        if (ids.isEmpty()) {
            return List.of();
        }
        return userRepository.findByIdsWithOffset(ids, from, size);
    }

    @Transactional
    public void delete(long userId) {
        checkUserExists(userId);
        userRepository.deleteById(userId);
    }

    // ===== helpers =====

    private void validatePagination(int from, int size) {
        if (from < 0 || size <= 0) {
            throw new BadRequestException("Incorrectly made request.");
        }
    }

    private void checkUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
    }
}