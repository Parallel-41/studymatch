package pt.upt.studymatch.profile.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.profile.application.ProfileService;

@RestController
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/api/students/{id}/profile")
    public ProfileResponse getProfile(@PathVariable long id) {
        return ProfileResponse.from(profileService.profileFor(id));
    }
}
