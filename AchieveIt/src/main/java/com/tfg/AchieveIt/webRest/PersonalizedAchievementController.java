package com.tfg.AchieveIt.webRest;

import com.tfg.AchieveIt.domain.PersonalizedAchievement;
import com.tfg.AchieveIt.domain.User;
import com.tfg.AchieveIt.domain.Videogame;
import com.tfg.AchieveIt.repository.LikeRepository;
import com.tfg.AchieveIt.repository.PersonalizedAchievementRepository;
import com.tfg.AchieveIt.repository.UserRepository;
import com.tfg.AchieveIt.repository.VideogameRepository;
import com.tfg.AchieveIt.services.PersonalizedAchievementService;
import com.tfg.AchieveIt.services.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class PersonalizedAchievementController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final VideogameRepository videogameRepository;
    private final PersonalizedAchievementRepository personalizedAchievementRepository;
    private final PersonalizedAchievementService personalizedAchievementService;
    private final LikeRepository likeRepository;

    public PersonalizedAchievementController(UserService userService, UserRepository userRepository, VideogameRepository videogameRepository, PersonalizedAchievementRepository personalizedAchievementRepository, PersonalizedAchievementService personalizedAchievementService, LikeRepository likeRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.videogameRepository = videogameRepository;
        this.personalizedAchievementRepository = personalizedAchievementRepository;
        this.personalizedAchievementService = personalizedAchievementService;
        this.likeRepository = likeRepository;
    }

    @GetMapping("/personalized-achievements")
    public List<PersonalizedAchievement> getAllPersonalizedAchievements() {

        return personalizedAchievementRepository.findAll();
    }

    @GetMapping("/personalized-achievements/{id}")
    public PersonalizedAchievement getPersonalizedAchievement(@PathVariable Long id) {
        return personalizedAchievementRepository.findById(id).orElseThrow(()-> new RuntimeException());
    }

    @DeleteMapping("/personalized-achievements/{id}")
    public void deletePersonalizedAchievement(@PathVariable Long id) {
        personalizedAchievementRepository.deleteById(id);
    }

    @GetMapping("/personalized-achievements/videogame/{id}")
    public List<PersonalizedAchievement> getPersonalizedAchievementByVideogame(@PathVariable("id") Long id) {
        return personalizedAchievementRepository.findPersonalizedAchievementByVideogameId(id);
    }

    @GetMapping("/personalized-achievements/videogame/{id}/user/{token}")
    public Set<Long> getUserPersonalizedAchievementsForGame(@PathVariable("id") String id, @PathVariable("token") String token) {

        Claims claims = Jwts.parserBuilder().setSigningKey(userService.getJwtSecret()).build().parseClaimsJws(token).getBody();
        String userId = claims.getSubject();
        Long uId = Long.parseLong(userId);

        Long videogameId = Long.parseLong(id);

        Optional<Videogame> OptVideogame = videogameRepository.findById(videogameId);
        if (OptVideogame.isPresent()) {
            return userRepository.findUserPersonalizedAchievementsForGame(uId, videogameId);
        } else {
            throw new RuntimeException("El videojuego no existe");
        }
    }

    @PostMapping("/personalized-achievements/add/{token}")
    @Transactional
    public void AddPersonalizedAchievement(@RequestBody Map<String, Long> requestBody, @PathVariable("token") String token){
        Claims claims = Jwts.parserBuilder().setSigningKey(userService.getJwtSecret()).build().parseClaimsJws(token).getBody();
        String userId = claims.getSubject();
        Long id = Long.parseLong(userId);
        Optional<User> currentUser = userRepository.findById(id);

        Long personalizedAchievementId = requestBody.get("personalizedAchievementId");

        Optional<PersonalizedAchievement> OptPersonalizedAchievement = personalizedAchievementRepository.findById(personalizedAchievementId);
        if (OptPersonalizedAchievement.isPresent()) {
            PersonalizedAchievement personalizedAchievement = OptPersonalizedAchievement.get();
            currentUser.get().addPersonalizedAchievement(personalizedAchievement);
        } else {
            throw new RuntimeException("El logro no existe");
        }
    }

    @PostMapping("/personalized-achievements/remove/{token}")
    @Transactional
    public void RemovePersonalizedAchievement(@RequestBody Map<String, Long> requestBody, @PathVariable("token") String token){
        Claims claims = Jwts.parserBuilder().setSigningKey(userService.getJwtSecret()).build().parseClaimsJws(token).getBody();
        String userId = claims.getSubject();
        Long id = Long.parseLong(userId);
        Optional<User> currentUser = userRepository.findById(id);

        Long personalizedAchievementId = requestBody.get("personalizedAchievementId");

        Optional<PersonalizedAchievement> OptPersonalizedAchievement = personalizedAchievementRepository.findById(personalizedAchievementId);
        if (OptPersonalizedAchievement.isPresent()) {
            PersonalizedAchievement personalizedAchievement = OptPersonalizedAchievement.get();
            currentUser.get().removePersonalizedAchievement(personalizedAchievement);
        } else {
            throw new RuntimeException("El logro no existe");
        }
    }
    @PostMapping("/personalized-achievements/create/{token}")
    @Transactional
    public void CreatePersonalizedAchievement(@RequestBody Map<String, String> requestBody, @PathVariable("token") String token){
        String name = requestBody.get("name");
        String description = requestBody.get("description");
        Long videogameId = Long.parseLong(requestBody.get("videogameId"));


        Claims claims = Jwts.parserBuilder().setSigningKey(userService.getJwtSecret()).build().parseClaimsJws(token).getBody();
        String userId = claims.getSubject();
        Long uId = Long.parseLong(userId);

        personalizedAchievementService.createPersonalizedAchievement(name, description, uId, videogameId);
    }

    @PostMapping("/personalized-achievements/{personalizedAchievementId}/like/{token}")
    @Transactional
    public void likePersonalizedAchievement(@PathVariable("personalizedAchievementId") String personalizedAchievementId, @PathVariable("token") String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(userService.getJwtSecret()).build().parseClaimsJws(token).getBody();
        String userId = claims.getSubject();
        Long uId = Long.parseLong(userId);

        Long personalizedAchievementIdL = Long.parseLong(personalizedAchievementId);

        Optional<PersonalizedAchievement> OptPersonalizedAchievement = personalizedAchievementRepository.findById(personalizedAchievementIdL);

        if(OptPersonalizedAchievement.isPresent()){
            PersonalizedAchievement personalizedAchievement = OptPersonalizedAchievement.get();
            personalizedAchievement.setLikesNum(personalizedAchievement.getLikesNum()+1);
            personalizedAchievementService.likePersonalizedAchievement(personalizedAchievementIdL, uId);
        }else{
            throw new RuntimeException("El logro personalizado no existe");
        }
    }

    @PostMapping("/personalized-achievements/{personalizedAchievementId}/dislike/{token}")
    @Transactional
    public void dislikePersonalizedAchievement(@PathVariable("personalizedAchievementId") String personalizedAchievementId, @PathVariable("token") String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(userService.getJwtSecret()).build().parseClaimsJws(token).getBody();
        String userId = claims.getSubject();
        Long uId = Long.parseLong(userId);

        Long personalizedAchievementIdL = Long.parseLong(personalizedAchievementId);

        Optional<PersonalizedAchievement> OptPersonalizedAchievement = personalizedAchievementRepository.findById(personalizedAchievementIdL);

        if(OptPersonalizedAchievement.isPresent()){
            PersonalizedAchievement personalizedAchievement = OptPersonalizedAchievement.get();
            personalizedAchievement.setLikesNum(personalizedAchievement.getLikesNum()-1);
            personalizedAchievementService.dislikePersonalizedAchievement(personalizedAchievementIdL, uId);
        }else{
            throw new RuntimeException("El logro personalizado no existe");
        }
    }

    @GetMapping("/personalized-achievements/{personalizedAchievementId}/liked-by/{token}")
    public boolean isLikedByUser(@PathVariable("personalizedAchievementId") String personalizedAchievementId, @PathVariable("token") String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(userService.getJwtSecret()).build().parseClaimsJws(token).getBody();
        String userId = claims.getSubject();
        Long uId = Long.parseLong(userId);

        Long personalizedAchievementIdL = Long.parseLong(personalizedAchievementId);

        Optional<PersonalizedAchievement> OptPersonalizedAchievement = personalizedAchievementRepository.findById(personalizedAchievementIdL);

        if(OptPersonalizedAchievement.isPresent()){
            return likeRepository.existsByPersonalizedAchievementIdAndUserId(personalizedAchievementIdL, uId);
        }else{
            throw new RuntimeException("El me gusta del logro personalizado no existe");
        }
    }
}
