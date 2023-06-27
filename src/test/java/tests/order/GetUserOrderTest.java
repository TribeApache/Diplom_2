package tests.order;

import api.model.User;
import api.steps.OrderSteps;
import api.steps.UserSteps;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class GetUserOrderTest {

    private String email;
    private String password;
    private String name;
    private UserSteps userSteps;
    private OrderSteps orderSteps;
    private User user;
    private String accessToken;

    @Before
    public void setUp() throws InterruptedException {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        name = RandomStringUtils.randomAlphanumeric(4, 20);
        email = RandomStringUtils.randomAlphanumeric(6, 10) + "@yandex.ru";
        password = RandomStringUtils.randomAlphanumeric(10, 20);
        userSteps = new UserSteps();
        orderSteps = new OrderSteps();
        user = new User(name, email, password);
        Response resp = userSteps.sendPostRequestApiAuthRegister(user);
        accessToken = JsonPath.from(resp.getBody().asString()).get("accessToken");
        Thread.sleep(200);
    }

    @Test
    @DisplayName("Getting the order list of an authorized user")
    @Description("Obtaining the list of orders of an authorized user. Checking the successful response from the server")
    public void getUserOrderWithAuthorizationTest() {
        Response response = orderSteps.getUserOrderWithAuthorization(accessToken);
        orderSteps.verifySuccessfulResponseForUserOrderWithAuthorization(response);
    }


    @Test
    @DisplayName("Getting a list of orders without authorization")
    @Description("Receiving a list of orders without authorization. Checking unsuccessful response from the server")
    public void getUserOrderWithoutAuthorizationTest() {
        Response response = orderSteps.getUserOrderWithoutAuthorization();
        orderSteps.verifyUnsuccessfulResponseForUserOrderWithoutAuthorization(response);
    }

    @After
    public void deleteRandomUser() {
        userSteps.deleteUser(accessToken);
    }
}
