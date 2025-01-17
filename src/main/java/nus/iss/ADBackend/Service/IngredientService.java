package nus.iss.ADBackend.Service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nus.iss.ADBackend.Repo.IngredientRepository;
import nus.iss.ADBackend.model.Ingredient;

@Service
public class IngredientService {
	@Autowired
	IngredientRepository iRepo;

	public void createIngredient(Ingredient ingredient) {
		iRepo.saveAndFlush(ingredient);
	}

	@Transactional
	public boolean saveIngredient(Ingredient ingredient) {
		if (iRepo.findById(ingredient.getId()) != null) {
			iRepo.saveAndFlush(ingredient);
			return true;
		}
		// if Ingredient not existed
		return false;
	}

	public List<Ingredient> findSimilarIngredients(String ingredientName) {
		return iRepo.findSimilarIngredientsByName(ingredientName.toLowerCase());
	}

	public List<Ingredient> findIngredientsWithCaloriesLowerThan(double val) {
		return iRepo.findAllByMaxCalories(val);
	}

	public List<Ingredient> getAllIngredient() {
		return iRepo.findAll();
	}

	public Ingredient findIngredientById(int id) {
		return iRepo.findById(id).orElse(null);
	}

	public Ingredient findIngredientByName(String ingredientName) {
		return iRepo.findByName(ingredientName.toLowerCase());
	}
}
