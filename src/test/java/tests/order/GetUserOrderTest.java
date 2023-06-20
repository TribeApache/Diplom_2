package tests.order;

import api.model.User;
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

import static io.restassured.RestAssured.given;

public class GetUserOrderTest {

    private String email;
    private String password;
    private String name;
    private UserSteps userSteps;
    private User user;
    private String accessToken;

    @Before
    public void setUp() throws InterruptedException {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        name = RandomStringUtils.randomAlphanumeric(4, 20);
        email = RandomStringUtils.randomAlphanumeric(6, 10) + "@yandex.ru";
        password = RandomStringUtils.randomAlphanumeric(10, 20);
        userSteps = new UserSteps();
        user = new User(name, email, password);
        Response resp = userSteps.sendPostRequestApiAuthRegister(user);
        accessToken = JsonPath.from(resp.getBody().asString()).get("accessToken");
        Thread.sleep(200);
    }

    @Test
    @DisplayName("Getting the order list of an authorized user")
    @Description("Obtaining the list of orders of an authorized user. Checking the successful response from the server")
    public void getUserOrderWithAuthorizationTest() {
        Response response = given().log().all()
                .header("Content-Type", "application/json")
                .header("authorization", accessToken)
                .when()
                .get("/api/orders");
        response.then().log().all()
                .assertThat().body("success", Matchers.is(true))
                .and().body("orders", Matchers.notNullValue())
                .and().body("total", Matchers.any(Integer.class))
                .and().body("totalToday", Matchers.any(Integer.class))
                .and().statusCode(200);
    }


    @Test
    @DisplayName("Getting a list of orders without authorization")
    @Description("Receiving a list of orders without authorization. Checking unsuccessful response from the server")
    public void getUserOrderWithoutAuthorizationTest() {
        Response response = given().log().all()
                .header("Content-Type", "application/json")
                .when()
                .get("/api/orders");
        response.then().log().all()
                .assertThat().body("success", Matchers.is(false))
                .and().body("message", Matchers.is("You should be authorised"))
                .and().statusCode(401);
    }

    @After
    public void deleteRandomUser() {
        given().log().all()
                .header("Content-Type", "application/json")
                .body(user)
                .delete("/api/auth/user");
    }
}
