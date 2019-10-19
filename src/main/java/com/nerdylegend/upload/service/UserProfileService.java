package com.nerdylegend.upload.service;

import com.nerdylegend.upload.profile.UserProfile;
import com.nerdylegend.upload.repository.UserProfileDataAccessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class UserProfileService {

    private final UserProfileDataAccessRepository userProfileDataAccessRepository;

    @Autowired
    public UserProfileService(UserProfileDataAccessRepository userProfileDataAccessRepository) {
        this.userProfileDataAccessRepository = userProfileDataAccessRepository;
    }

    public List<UserProfile> getUserProfiles() {
        return userProfileDataAccessRepository.getUserProfiles();
    }

    public void uploadUserProfileImage(UUID userProfileId, MultipartFile file) {
        //1.check if image is not empty
        //2. If file is an image
        //3. The user exists in DB
        //4. Grab some metadata from file if any
        //5. store image in s3 and update the user profile with (userProfileImageLink) in the DB
    }
}
