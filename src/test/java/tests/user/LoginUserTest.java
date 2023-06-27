package tests.user;

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

import java.util.Locale;

import static io.restassured.RestAssured.given;

public class LoginUserTest {

    private String email;
    private String password;
    private String name;
    private UserSteps userSteps;
    private User user;
    private User authUser;

    private String accessToken;

    @Before
    public void setUp() throws InterruptedException {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        name = RandomStringUtils.randomAlphanumeric(4, 20);
        email = RandomStringUtils.randomAlphanumeric(6, 10) + "@yandex.ru";
        password = RandomStringUtils.randomAlphanumeric(10, 20);
        userSteps = new UserSteps();
        user = new User(name, email, password);
        authUser = new User();
        userSteps.sendPostRequestApiAuthRegister(user);
        Response resp = userSteps.sendPostRequestApiAuthRegister(user);
        accessToken = JsonPath.from(resp.getBody().asString()).get("accessToken");
        Thread.sleep(200);
    }

    @Test
    @DisplayName("User Authentication")
    @Description("Authorization of the user with registered random data. Verifying a successful server response")
    public void authorizationTest() {
        Response response = userSteps.sendPostRequestApiAuthLogin(user);
        response.then().log().all()
                .assertThat().body("success", Matchers.is(true))
                .and().body("accessToken", Matchers.notNullValue())
                .and().body("refreshToken", Matchers.notNullValue())
                .and().body("user.email", Matchers.is(user.getEmail().toLowerCase(Locale.ROOT)))
                .and().body("user.name", Matchers.is(name))
                .and().statusCode(200);
    }

    @Test
    @DisplayName("User authorization without E-mail(s)")
    @Description("Authorizing a user without E-mail(s). Checking unsuccessful server response")
    public void authorizationWithoutEmailTest() {
        authUser.setPassword(password);
        Response response = userSteps.sendPostRequestApiAuthLogin(authUser);
        userSteps.checkFailedResponseApiAuthLogin(response);

    }

    @Test
    @DisplayName("Authorizing a user without a password")
    @Description("Authorizing a user without a password. Checking unsuccessful server response")
    public void authorizationWithoutPasswordTest() {
        authUser.setEmail(email);
        Response response = userSteps.sendPostRequestApiAuthLogin(authUser);
        userSteps.checkFailedResponseApiAuthLogin(response);
    }

    @Test
    @DisplayName("User authorization without E-mail(s) and password")
    @Description("Authorization of a user without email(s) and password. Checking unsuccessful server response")
    public void authorizationWithoutEmailAndPasswordTest() {
        Response response = userSteps.sendPostRequestApiAuthLogin(authUser);
        userSteps.checkFailedResponseApiAuthLogin(response);
    }

    @Test
    @DisplayName("Authorizing a user with an invalid E-mail(s)")
    @Description("Authorize user with invalid E-mail(s) and with valid random password and name " +
            "Verification of unsuccessful server response")
    public void authorizationWithWrongEmailTest() {
        authUser = new User(name, email, password);
        authUser.setEmail("haha" + email);
        Response response = userSteps.sendPostRequestApiAuthLogin(authUser);
        userSteps.checkFailedResponseApiAuthLogin(response);
    }

    @Test
    @DisplayName("Authorizing a user with an invalid password")
    @Description("Authorizing a user with a non-valid password and a valid random E-mail and name " +
            "Verification of unsuccessful server response")
    public void authorizationWithWrongPasswordTest() {
        authUser = new User(name, email, password);
        authUser.setPassword(password + "8765");
        Response response = userSteps.sendPostRequestApiAuthLogin(authUser);
        userSteps.checkFailedResponseApiAuthLogin(response);
    }

    @After
    public void deleteRandomUser() {
        userSteps.deleteUser(accessToken);
    }
}
