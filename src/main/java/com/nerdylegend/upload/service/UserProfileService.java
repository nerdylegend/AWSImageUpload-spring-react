package com.nerdylegend.upload.service;

import com.nerdylegend.upload.bucket.BucketName;
import com.nerdylegend.upload.filestore.FileStore;
import com.nerdylegend.upload.profile.UserProfile;
import com.nerdylegend.upload.repository.UserProfileDataAccessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.apache.http.entity.ContentType.*;

@Service
public class UserProfileService {

    private final UserProfileDataAccessRepository userProfileDataAccessRepository;

    private final FileStore fileStore;

    @Autowired
    public UserProfileService(UserProfileDataAccessRepository userProfileDataAccessRepository, FileStore fileStore) {
        this.userProfileDataAccessRepository = userProfileDataAccessRepository;
        this.fileStore = fileStore;
    }

    public List<UserProfile> getUserProfiles() {
        return userProfileDataAccessRepository.getUserProfiles();
    }

    public void uploadUserProfileImage(UUID userProfileId, MultipartFile file) {
        //1.check if image is not empty
        isFileEmpty(file);

        //2. If file is an image
        isImage(file);

        //3. The user exists in DB
        var user = getUserProfile(userProfileId);

        //4. Grab some metadata from file if any
        Map<String, String> metadata = extractMetaData(file);

        //5. store image in s3 and update the user profile with (userProfileImageLink) in the DB
        String path = String.format("%s/%s", BucketName.PROFILE_NAME.getBucketName(), user.getUserProfileId());
        String fileName = String.format("%s-%s", file.getOriginalFilename(), UUID.randomUUID());

        try {
            fileStore.save(path, fileName, Optional.of(metadata), file.getInputStream());
            user.setUserProfileImageLink(fileName);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<String, String> extractMetaData(MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        return metadata;
    }

    private UserProfile getUserProfile(UUID userProfileId) {
        return userProfileDataAccessRepository
                .getUserProfiles()
                .stream()
                .filter(userProfile ->
                        userProfile.getUserProfileId().equals(userProfileId)
                )
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("User profile %s not found", userProfileId)));
    }

    private void isImage(MultipartFile file) {
        if (!List.of(IMAGE_JPEG.getMimeType(),
                IMAGE_PNG.getMimeType(),
                IMAGE_GIF.getMimeType()).contains(file.getContentType())) {
            throw new IllegalStateException("File must be an Image [ "
                    + file.getContentType() + " ]");
        }
    }

    private void isFileEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalStateException("Cannot upload empty file [ " + file.getSize() + " ]");
        }
    }

    public byte[] downloadUserProfileImage(UUID userProfileId) {
        var user = getUserProfile(userProfileId);

        String path = String.format("%s/%s",
                BucketName.PROFILE_NAME.getBucketName(),
                user.getUserProfileId());

        return user.getUserProfileImageLink()
                .map(key -> fileStore.download(path, key))
                .orElse(new byte[0]);
    }
}
