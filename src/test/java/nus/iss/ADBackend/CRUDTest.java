package nus.iss.ADBackend;


import nus.iss.ADBackend.Repo.*;
import nus.iss.ADBackend.model.*;
import org.hibernate.type.descriptor.java.LocalDateTimeJavaDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CRUDTest {

    @Autowired
    DishRepository dRepo;
    @Autowired
    NutritionRecordRepository nRepo;
    @Autowired
    UserRepository uRepo;

    @Autowired
    RecipeRepository rRepo;

    @Autowired
    CommentRepository cRepo;
    public Dish createDishWithNutritionInfo() {
        NutritionRecord n = new NutritionRecord(10.0);
        Dish dish = new Dish("MALA", "path", 100.0, n);
        return dish;
    }
    @Test
    @Order(1)
    public void DishCreateTest() {
        //Create Test
        Dish dish = createDishWithNutritionInfo();
        dRepo.saveAndFlush(dish);
        Dish d = dRepo.findByName("MALA");
        Assertions.assertNotNull(d);
        System.out.println(d.getCalorie());
        NutritionRecord nr = d.getNutritionRecord();
        Assertions.assertNotNull(nr);
        //Update the Value
        d.setName("FISH");
        dRepo.save(d);
        Assertions.assertNull(dRepo.findByName("MALA"));
        Assertions.assertNotNull(dRepo.findByName("FISH"));
        Assertions.assertEquals(dRepo.findByName("FISH").getNutritionRecord().getTotalCalories(), 10.0);
    }
    @Test
    @Order(2)
    public void DishDeleteTest() {
        //Delete Test
        Dish dish = createDishWithNutritionInfo();
        dRepo.saveAndFlush(dish);
        Dish d = dRepo.findByName("MALA");
        int nId = d.getNutritionRecord().getId();
        Assertions.assertEquals(nRepo.findById(nId).getTotalCalories(), 10.0);
        dRepo.delete(d);
        Assertions.assertNull(dRepo.findByName("MALA"));
        Assertions.assertNull(nRepo.findById(nId));
    }

    private User createUserByName(String name, Role role) {
        User u = new User(name, "test-username", "test-password", role);
        uRepo.save(u);
        return uRepo.findByName(name);
    }

    private Recipe createRecipeByUserAndDish(User user, Dish dish, LocalDateTime time, String stpes) {
        Recipe r = new Recipe("imagetestpath", user, dish, time, stpes);
        rRepo.saveAndFlush(r);
        return rRepo.findByUserIdAndAndDateTime(user.getId(), time);
    }

    @Test
    @Order(3)
    public void recipeCreationAndDeleteTest() {
        dRepo.saveAndFlush(createDishWithNutritionInfo());
        Dish d = dRepo.findByName("MALA");
        User u1 = createUserByName("test-staff", Role.NORMAL);
        User u2 = createUserByName("test-admin", Role.ADMIN);
        //public Recipe(String image, User user, Dish dish, LocalDateTime dateTime, String procedures)
        LocalDateTime t1 = LocalDateTime.of(2020, 12, 31, 11, 59, 59);
        LocalDateTime t2 = LocalDateTime.of(2021, 12, 31, 11, 59, 59);
        Recipe r1 = createRecipeByUserAndDish(u1, d, t1, "11111");
        Recipe r2 = createRecipeByUserAndDish(u2, d, t2, "22222");
        Assertions.assertNotNull(rRepo.findByUserIdAndAndDateTime(u1.getId(), t1));
        Assertions.assertNotNull(rRepo.findByUserIdAndAndDateTime(u2.getId(), t2));
        Assertions.assertNull(rRepo.findByUserIdAndAndDateTime(u1.getId(), t2));
        Assertions.assertEquals(2, rRepo.findAllByDishId(d.getId()).size());
        /*
        List<Recipe> rList = rRepo.findAllByDishId(d.getId());
        for (Recipe r: rList)
        {
            System.out.println(r);
        }*/
        rRepo.deleteByUserId(u1.getId());
        Assertions.assertEquals(1, rRepo.findAllByDishId(d.getId()).size());
        rRepo.deleteByDishId(d.getId());
        Assertions.assertEquals(0, rRepo.findAllByDishId(d.getId()).size());
        Assertions.assertNull(rRepo.findByUserIdAndAndDateTime(u1.getId(), t1));
        Assertions.assertNull(rRepo.findByUserIdAndAndDateTime(u2.getId(), t2));
        Assertions.assertNotNull(dRepo.findByName("MALA"));
    }
    //public Comment(double rating, String content, User user, LocalDateTime dateTime, Recipe recipe)
    private Comment createComment(double rating, String content, User user, LocalDateTime dateTime, Recipe recipe) {
        Comment c = new Comment(rating, content, user, dateTime, recipe);
        cRepo.saveAndFlush(c);
        return cRepo.findByUserIdAndDateTime(user.getId(), dateTime);
    }

    @Test
    @Order(4)
    public void CommentCreateAndDeleteTest() {
        dRepo.saveAndFlush(createDishWithNutritionInfo());
        Dish d = dRepo.findByName("MALA");
        User u1 = createUserByName("test-staff", Role.NORMAL);
        User u2 = createUserByName("test-admin", Role.ADMIN);
        LocalDateTime t1 = LocalDateTime.of(2020, 12, 31, 11, 59, 59);
        LocalDateTime t2 = LocalDateTime.of(2021, 12, 31, 11, 59, 59);
        Recipe r1 = createRecipeByUserAndDish(u1, d, t1, "11111");
        Comment c1 = createComment(5.0, "nice", u1, t1, r1);
        Comment c2 = createComment(1.0, "nice", u2, t2, r1);
        List<Comment> commentList = cRepo.findAllByRecipeId(r1.getId());
        Assertions.assertEquals(2, commentList.size());
        for (Comment c : commentList) {
            System.out.println(c);
        }
        //delete dish and it associate nutritution info, recipe and comment
        //when perform the parent delet operation we shall delete the children first and then followed by
        cRepo.deleteAllByRecipeId(r1.getId());//delete comment
        rRepo.deleteByDishId(d.getId());
        dRepo.delete(d);
        Assertions.assertNull(rRepo.findByUserIdAndAndDateTime(r1.getUser().getId(), r1.getDateTime()));
        Assertions.assertEquals(0, cRepo.findAllByRecipeId(r1.getId()).size());
        Assertions.assertNull(dRepo.findByName("MALA"));
    }

}
