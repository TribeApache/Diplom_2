package tests.order;

import api.model.Ingredient;
import api.model.Ingredients;
import api.model.Order;
import api.model.User;
import api.steps.OrderSteps;
import api.steps.UserSteps;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static io.restassured.RestAssured.given;


public class CreateOrderTest {

    private String name;
    private String email;
    private String password;
    private UserSteps userSteps;
    private User user;
    private String accessToken;
    private OrderSteps orderSteps;
    private List<String> ingr;
    private Order order;


    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        name = RandomStringUtils.randomAlphanumeric(4, 20);
        email = RandomStringUtils.randomAlphanumeric(6, 10) + "@yandex.ru";
        password = RandomStringUtils.randomAlphanumeric(10, 20);
        userSteps = new UserSteps();
        orderSteps = new OrderSteps();
        user = new User(name, email, password);
        Response resp = userSteps.sendPostRequestApiAuthRegister(user);
        accessToken = JsonPath.from(resp.getBody().asString()).get("accessToken");
        ingr = new ArrayList<>();
        order = new Order(ingr);
    }

    @Test
    @DisplayName("Creating an order without authorization")
    @Description("Creating an order without authorization. Checking the successful response from the server")
    public void createOrderWithoutAuthorizationTest() {
        Ingredients ingredients = orderSteps.getIngredients();
        ingr.add(ingredients.getData().get(0).get_id());
        ingr.add(ingredients.getData().get(8).get_id());
        ingr.add(ingredients.getData().get(4).get_id());
        Response response = orderSteps.createOrderWithoutAuthorization(order);
        response.then().log().all()
                .assertThat().body("success", Matchers.is(true))
                .and().body("name", Matchers.notNullValue())
                .and().body("order.number", Matchers.any(Integer.class))
                .and().statusCode(200);
    }

    @Test
    @DisplayName("Creating an order with authorization")
    @Description("Creating an order with authorization. Checking the successful response from the server")
    public void createOrderWithAuthorizationTest() throws InterruptedException {
        Ingredients ingredients = orderSteps.getIngredients();
        Thread.sleep(100);
        ingr.add(ingredients.getData().get(1).get_id());
        ingr.add(ingredients.getData().get(3).get_id());
        ingr.add(ingredients.getData().get(5).get_id());
        int sumPrice = ingredients.getData().stream().filter(ingredient -> ingr.contains(ingredient.get_id()))
                .map(Ingredient::getPrice).mapToInt(i -> i).sum();
        Response response = orderSteps.createOrderWithAuthorization(order, accessToken);
        response.then().log().all()
                .assertThat().body("success", Matchers.is(true))
                .and().body("name", Matchers.notNullValue())
                .and().body("order.number", Matchers.any(Integer.class))
                .and().body("order.ingredients", Matchers.notNullValue())
                .and().body("order._id", Matchers.notNullValue())
                .and().body("order.owner.name", Matchers.is(name))
                .and().body("order.owner.email", Matchers.is(email.toLowerCase(Locale.ROOT)))
                .and().body("order.status", Matchers.is("done"))
                .and().body("order.name", Matchers.notNullValue())
                .and().body("order.price", Matchers.is(sumPrice))
                .and().statusCode(200);
    }

    @Test
    @DisplayName("Creating an order without ingredients and without authorization")
    @Description("Creating an order without ingredients and without authorization. Checking an unsuccessful response from the server")
    public void createEmptyOrderWithoutAuthorization() {
        Response response = orderSteps.createOrderWithoutAuthorization(order);
        orderSteps.checkFailedResponseApiOrders(response);
    }

    @Test
    @DisplayName("Creating an order without ingredients with authorization")
    @Description("Creating an order without ingredients with authorization. Checking an unsuccessful response from the server")
    public void createEmptyOrderWithAuthorization() throws InterruptedException {
        Thread.sleep(100);
        Response response = orderSteps.createOrderWithAuthorization(order, accessToken);
        orderSteps.checkFailedResponseApiOrders(response);
    }

    @Test
    @DisplayName("Creating an order without authorization with an invalid hash of ingredients")
    @Description("Creating an order without authorization with an invalid ingredient hash. Checking server error")
    public void createOrderWithoutAuthorizationWithWrongHashTest() throws InterruptedException {
        Ingredients ingredients = orderSteps.getIngredients();
        Thread.sleep(100);
        ingr.add(ingredients.getData().get(0).get_id() + "1234nhf8");
        ingr.add(ingredients.getData().get(8).get_id() + "9876bgfj2");
        Response response = orderSteps.createOrderWithoutAuthorization(order);
        response.then().log().all()
                .statusCode(500);
    }

    @Test
    @DisplayName("Creating an order with authorization with an invalid hash of ingredients")
    @Description("Creating an order with authorization with an invalid hash of ingredients. Checking server error")
    public void createOrderWithAuthorizationWithWrongHashTest() {
        Ingredients ingredients = orderSteps.getIngredients();
        ingr.add(ingredients.getData().get(1).get_id() + "1234nhf8");
        ingr.add(ingredients.getData().get(2).get_id() + "9876bgfj2");
        Response response = orderSteps.createOrderWithAuthorization(order, accessToken);
        response.then().log().all()
                .statusCode(500);
    }

    @After
    public void deleteRandomUser() {
        given().log().all()
                .header("Content-Type", "application/json")
                .body(user)
                .delete("/api/auth/user");
    }
}