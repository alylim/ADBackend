package nus.iss.ADBackend.controller;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.minidev.json.JSONObject;
import nus.iss.ADBackend.Service.UserService;
import nus.iss.ADBackend.model.Goal;
import nus.iss.ADBackend.model.Role;
import nus.iss.ADBackend.model.User;

@RestController
@RequestMapping(value = "/register", produces = "application/json")
public class registerController {

	@Autowired
	UserService userService;

	@RequestMapping("/validateNewUser")
	public User validateNewUser(@RequestBody JSONObject requestBody) throws IOException, ParseException {
		User user = userService.findUserByUsername(requestBody.getAsString("email"));

		if (user == null) {
			User newUser = new User(requestBody.getAsString("email"), requestBody.getAsString("password"));
			String dob = requestBody.getAsString("dob");
			System.out.println(dob);
			newUser.setDateofbirth(getDate(dob));
			String gender = requestBody.getAsString("gender");
			System.out.println(gender);
			newUser.setGender(gender);
			String goal = requestBody.getAsString("goal");
			System.out.println(goal);
			newUser.setGoal(getGoal(goal));

			userService.createUser(newUser);
			System.out.println(newUser);
			return newUser;
		}
		return null;
	}

	private Goal getGoal(String goal) {
		switch (goal) {
		case "WEIGHTLOSS":
			return Goal.WEIGHTLOSS;
		case "WEIGHTGAIN":
			return Goal.WEIGHTGAIN;
		case "WEIGHTMAINTAIN":
			return Goal.WEIGHTMAINTAIN;
		case "MUSCULE":
			return Goal.MUSCLE;

		// should not come to this default
		default:
			return Goal.WEIGHTMAINTAIN;
		}
	}

	private LocalDate getDate(String dob) {
		LocalDate date;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		date = LocalDate.parse(dob, formatter);
		return date;
	}

	@RequestMapping("/updateUserDetails")
	public User updateUserDetails(@RequestBody JSONObject requestBody) throws IOException, ParseException {

		User updatedUser = new User();

		updatedUser.setId(Integer.parseInt(requestBody.getAsString("id")));
		updatedUser.setName(requestBody.getAsString("name"));
		updatedUser.setUsername(requestBody.getAsString("username"));
		updatedUser.setPassword(requestBody.getAsString("password"));
		updatedUser.setDateofbirth(LocalDate.parse(requestBody.getAsString("dateofbirth")));
		updatedUser.setGender(requestBody.getAsString("gender"));
		updatedUser.setGoal(Goal.valueOf(requestBody.getAsString("goal")));
		updatedUser.setCalorieintake_limit_inkcal(
				Double.parseDouble(requestBody.getAsString("calorieintake_limit_inkcal")));
		updatedUser.setWaterintake_limit_inml(Double.parseDouble(requestBody.getAsString("waterintake_limit_inml")));
		updatedUser.setRole(Role.NORMAL);
		
		if (userService.saveUser(updatedUser)) {
			return userService.findUserByUserNameAndPassword(updatedUser.getUsername(), updatedUser.getPassword());
		}

		return null;

	}

}
