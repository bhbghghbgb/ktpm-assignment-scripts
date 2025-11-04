package org.example.flogin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RED: AuthService Validation Unit Tests")
public class AuthServiceValidationTest {

    // Không cần mock vì chỉ test phương thức validate nội bộ, độc lập
    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Khởi tạo các mock (mặc dù không dùng trong test này, nhưng là thói quen tốt)
        MockitoAnnotations.openMocks(this);
    }

    // --- Tests cho Validation (TDD RED) ---

    @Test
    @DisplayName("RED: validateUser - Nên thất bại khi username rỗng")
    void validateUser_ShouldFail_WhenUsernameIsEmpty() {
        // Arrange
        String invalidUsername = "";
        String validPassword = "ValidPassword123";

        // Act & Assert (Assert là False cho test RED hiện tại vì code chưa validate)
        // Hiện tại code có thể trả về true vì chỉ có check null và length < 5
        boolean result = authService.validateUser(invalidUsername, validPassword);

        // Theo yêu cầu validation: Username phải có nội dung
        assertFalse(result, "Username rỗng phải fail validation.");
    }

    @Test
    @DisplayName("RED: validateUser - Nên thất bại khi username < 5 ký tự")
    void validateUser_ShouldFail_WhenUsernameIsTooShort() {
        // Arrange
        String shortUsername = "test"; // 4 ký tự
        String validPassword = "ValidPassword123";

        // Act & Assert
        boolean result = authService.validateUser(shortUsername, validPassword);
        assertFalse(result, "Username ngắn hơn 5 ký tự phải fail validation.");
    }

    @Test
    @DisplayName("RED: validateUser - Nên thất bại khi password < 8 ký tự")
    void validateUser_ShouldFail_WhenPasswordIsTooShort() {
        // Arrange
        String validUsername = "testuser";
        String shortPassword = "short"; // 5 ký tự

        // Act & Assert
        boolean result = authService.validateUser(validUsername, shortPassword);
        assertFalse(result, "Password ngắn hơn 8 ký tự phải fail validation.");
    }

    // Yêu cầu: Username phải dài >= 6 ký tự.
    // RED: Test case thất bại khi username quá ngắn hoặc null
    @Test
    void testValidateUsername_ShouldFailWhenTooShort() {
        // 1. Username null
        assertFalse(authService.validateUser(null, "Test1234"), "RED: Test should fail for null username");
        // 2. Username quá ngắn (5 ký tự)
        assertFalse(authService.validateUser("user5", "Test1234"), "RED: Test should fail for username < 6 chars");
    }

    // Yêu cầu: Password phải dài >= 8 ký tự, có ít nhất 1 ký tự hoa, 1 ký tự thường, 1 số, 1 ký tự đặc biệt.
    // RED: Test case thất bại khi password thiếu các yêu cầu complexity
    @Test
    void testValidatePassword_ShouldFailWhenNotComplex() {
        // 1. Password null
        assertFalse(authService.validateUser("testuser", null), "RED: Test should fail for null password");
        // 2. Password quá ngắn (7 ký tự)
        assertFalse(authService.validateUser("testuser", "Short7!"), "RED: Test should fail for password < 8 chars");
        // 3. Thiếu ký tự hoa
        assertFalse(authService.validateUser("testuser", "tes1234!"), "RED: Test should fail without uppercase");
        // 4. Thiếu ký tự thường
        assertFalse(authService.validateUser("testuser", "TEST1234!"), "RED: Test should fail without lowercase");
        // 5. Thiếu số
        assertFalse(authService.validateUser("testuser", "Testuser!"), "RED: Test should fail without digit");
        // 6. Thiếu ký tự đặc biệt
        assertFalse(authService.validateUser("testuser", "Test1234"), "RED: Test should fail without special char");
    }

    // GREEN: Test case thành công (sẽ thất bại trong bước RED hiện tại vì logic chưa có)
    @Test
    void testValidateCredentials_ShouldPassWhenValid() {
        // Test này sẽ *thất bại* ở bước RED vì code hiện tại chỉ kiểm tra độ dài cơ bản
        assertTrue(authService.validateUser("testuser", "Test1234!"), "RED: Test should fail until logic is implemented");
    }
}