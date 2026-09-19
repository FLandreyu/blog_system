package com.sunyongjie.blog.entity;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link User}, the Lombok {@code @Data} data holder that maps
 * the {@code user} table.
 *
 * <p>
 * These tests pin the accessor and value-semantics behaviour the application
 * relies on when a
 * user is loaded by MyBatis and passed around the service layer. No Spring
 * context, no database and
 * no network access are required, so the tests stay deterministic.
 */
class UserTest {

    // Sample values live in constants to keep assertions readable and
    // deterministic.
    private static final Long USER_ID = 42L;
    private static final String USERNAME = "alice";
    private static final String PASSWORD_HASH = "$2a$10$abcdefghijklmnopqrstuvABCDEFGHIJKLMNOPqrstuv";
    private static final String NICKNAME = "Alice";
    private static final String EMAIL = "alice@example.com";
    private static final String AVATAR = "https://example.com/avatar.png";
    private static final String ROLE_USER = "USER";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final LocalDateTime CREATE_TIME = LocalDateTime.of(2026, 1, 2, 3, 4, 5);
    private static final LocalDateTime UPDATE_TIME = LocalDateTime.of(2026, 6, 7, 8, 9, 10);

    private static User sampleUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD_HASH);
        user.setNickname(NICKNAME);
        user.setEmail(EMAIL);
        user.setAvatar(AVATAR);
        user.setRole(ROLE_USER);
        user.setCreateTime(CREATE_TIME);
        user.setUpdateTime(UPDATE_TIME);
        return user;
    }

    @Test
    @DisplayName("A new User has every field unset")
    void newUserHasAllFieldsUnset() {
        User user = new User();

        assertAll(
                () -> assertNull(user.getId()),
                () -> assertNull(user.getUsername()),
                () -> assertNull(user.getPassword()),
                () -> assertNull(user.getNickname()),
                () -> assertNull(user.getEmail()),
                () -> assertNull(user.getAvatar()),
                () -> assertNull(user.getRole()),
                () -> assertNull(user.getCreateTime()),
                () -> assertNull(user.getUpdateTime()));
    }

    @Test
    @DisplayName("Getters return exactly what the setters stored")
    void gettersRoundTripSetterValues() {
        User user = sampleUser();

        assertAll(
                () -> assertEquals(USER_ID, user.getId()),
                () -> assertEquals(USERNAME, user.getUsername()),
                () -> assertEquals(PASSWORD_HASH, user.getPassword()),
                () -> assertEquals(NICKNAME, user.getNickname()),
                () -> assertEquals(EMAIL, user.getEmail()),
                () -> assertEquals(AVATAR, user.getAvatar()),
                () -> assertEquals(ROLE_USER, user.getRole()),
                () -> assertEquals(CREATE_TIME, user.getCreateTime()),
                () -> assertEquals(UPDATE_TIME, user.getUpdateTime()));
    }

    @Test
    @DisplayName("Two users with identical fields are equal and share a hash code")
    void usersWithSameFieldsAreEqual() {
        User first = sampleUser();
        User second = sampleUser();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    @DisplayName("Changing any single field breaks equality")
    void differenceInAnyFieldBreaksEquality() {
        User base = sampleUser();

        User differentId = sampleUser();
        differentId.setId(USER_ID + 1);

        User differentUsername = sampleUser();
        differentUsername.setUsername("bob");

        User differentPassword = sampleUser();
        differentPassword.setPassword("$2a$10$anotherHash");

        User differentNickname = sampleUser();
        differentNickname.setNickname("Alice B.");

        User differentEmail = sampleUser();
        differentEmail.setEmail("other@example.com");

        User differentAvatar = sampleUser();
        differentAvatar.setAvatar(null);

        User differentRole = sampleUser();
        differentRole.setRole(ROLE_ADMIN);

        User differentCreateTime = sampleUser();
        differentCreateTime.setCreateTime(CREATE_TIME.plusDays(1));

        User differentUpdateTime = sampleUser();
        differentUpdateTime.setUpdateTime(UPDATE_TIME.plusDays(1));

        assertAll(
                () -> assertNotEquals(base, differentId),
                () -> assertNotEquals(base, differentUsername),
                () -> assertNotEquals(base, differentPassword),
                () -> assertNotEquals(base, differentNickname),
                () -> assertNotEquals(base, differentEmail),
                () -> assertNotEquals(base, differentAvatar),
                () -> assertNotEquals(base, differentRole),
                () -> assertNotEquals(base, differentCreateTime),
                () -> assertNotEquals(base, differentUpdateTime));
    }

    @Test
    @DisplayName("Equality is reflexive and never matches null or an unrelated type")
    void equalityHandlesEdgeCases() {
        User user = sampleUser();

        assertEquals(user, user);
        assertNotEquals(user, null);
        assertNotEquals(user, "alice");
        assertEquals(sampleUser().hashCode(), sampleUser().hashCode());
    }

    @Test
    @DisplayName("toString exposes the identifying fields for logging")
    void toStringExposesIdentifyingFields() {
        User user = sampleUser();
        String rendered = user.toString();

        assertAll(
                () -> assertTrue(rendered.contains("User"), rendered),
                () -> assertTrue(rendered.contains(USERNAME), rendered),
                () -> assertTrue(rendered.contains(EMAIL), rendered),
                () -> assertTrue(rendered.contains(ROLE_USER), rendered));
    }
}
