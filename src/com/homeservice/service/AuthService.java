package com.homeservice.service;

import com.homeservice.dao.ServiceProviderDAO;
import com.homeservice.dao.UserDAO;
import com.homeservice.model.Role;
import com.homeservice.model.ServiceProviderProfile;
import com.homeservice.model.User;

public class AuthService {
    private static User currentUser = null;
    private static ServiceProviderProfile currentProviderProfile = null;

    private final UserDAO userDAO = new UserDAO();
    private final ServiceProviderDAO providerDAO = new ServiceProviderDAO();

    public static User getCurrentUser() {
        return currentUser;
    }

    public static ServiceProviderProfile getCurrentProviderProfile() {
        return currentProviderProfile;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void setCurrentProviderProfile(ServiceProviderProfile profile) {
        currentProviderProfile = profile;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
        currentProviderProfile = null;
    }

    public User login(String email, String password) {
        User user = userDAO.authenticate(email, password);
        if (user != null) {
            if (!user.isActive()) {
                throw new RuntimeException("Your account is currently inactive. Please contact administration.");
            }
            currentUser = user;
            if (user.getRole() == Role.PROVIDER) {
                currentProviderProfile = providerDAO.findByUserId(user.getId());
            }
            return user;
        }
        return null;
    }

    public User registerCustomer(String fullName, String email, String password, String phone, String address) {
        if (userDAO.findByEmail(email) != null) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhone(phone);
        user.setRole(Role.CUSTOMER);
        user.setAddress(address);
        user.setStatus("ACTIVE");

        int id = userDAO.create(user);
        if (id > 0) {
            user.setId(id);
            currentUser = user;
            return user;
        }
        return null;
    }

    public User registerProvider(String fullName, String email, String password, String phone, String address,
                                 int categoryId, int experienceYears, double hourlyRate, String bio) {
        if (userDAO.findByEmail(email) != null) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhone(phone);
        user.setRole(Role.PROVIDER);
        user.setAddress(address);
        user.setStatus("ACTIVE");

        int userId = userDAO.create(user);
        if (userId > 0) {
            user.setId(userId);
            ServiceProviderProfile profile = new ServiceProviderProfile();
            profile.setUserId(userId);
            profile.setCategoryId(categoryId);
            profile.setExperienceYears(experienceYears);
            profile.setHourlyRate(hourlyRate);
            profile.setRatingAvg(5.0);
            profile.setTotalReviews(0);
            profile.setBio(bio);
            profile.setAvailable(true);
            providerDAO.createProfile(profile);

            currentUser = user;
            currentProviderProfile = providerDAO.findByUserId(userId);
            return user;
        }
        return null;
    }
}
