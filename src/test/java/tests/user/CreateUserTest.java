package tests.user;

import api.model.User;
import api.steps.UserSteps;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static io.restassured.RestAssured.given;

public class CreateUserTest {

    private String name;
    private String email;
    private String password;
    private UserSteps userSteps;
    private User user;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }

    @Before
    public void createRandomData() throws InterruptedException {
        name = RandomStringUtils.randomAlphanumeric(4, 20);
        email = RandomStringUtils.randomAlphanumeric(6, 10) + "@yandex.ru";
        password = RandomStringUtils.randomAlphanumeric(10, 20);
        userSteps = new UserSteps();
        user = new User();
        Thread.sleep(200);
    }


    @Test
    @DisplayName("Registration of a unique user")
    @Description("Registration of a unique user with a random set of data. Checking for a successful server response")
    public void createUserTest() {
        user = new User(name, email, password);
        Response response = userSteps.sendPostRequestApiAuthRegister(user);
        response.then().log().all()
                .assertThat().body("success", Matchers.is(true))
                .and().body("user.email", Matchers.is(email.toLowerCase(Locale.ROOT)))
                .and().body("user.name", Matchers.is(name))
                .and().body("accessToken", Matchers.notNullValue())
                .and().body("refreshToken", Matchers.notNullValue())
                .and().statusCode(200);
    }


    @Test
    @DisplayName("Registering an already created user")
    @Description("Registration of an already created user with a random set of data. Checking for unsuccessful server response")
    public void createTwoIdenticalUsersTest() throws InterruptedException {
        user = new User(name, email, password);
        userSteps.sendPostRequestApiAuthRegister(user);
        Thread.sleep(100);
        Response response = userSteps.sendPostRequestApiAuthRegister(user);
        response.then().log().all()
                .assertThat().body("success", Matchers.is(false))
                .and().body("message", Matchers.is("User already exists"))
                .and().statusCode(403);
    }

    @Test
    @DisplayName("Registering a user without a name")
    @Description("Registering a user without a name, but with a random e-mail and password. Checking for unsuccessful server response")
    public void createUserWithoutNameTest() {
        user.setEmail(email);
        user.setPassword(password);
        Response response = userSteps.sendPostRequestApiAuthRegister(user);
        userSteps.checkFailedResponseApiAuthRegister(response);
    }

    @Test
    @DisplayName("User registration without E-mail")
    @Description("Registering a user without E-mail, but with a random username and password. Checking for unsuccessful server response")
    public void createUserWithoutEmailTest() {
        user.setName(name);
        user.setPassword(password);
        Response response = userSteps.sendPostRequestApiAuthRegister(user);
        userSteps.checkFailedResponseApiAuthRegister(response);
    }

    @Test
    @DisplayName("Registering a user without a password")
    @Description("Registering a user without a password, but with a random E-mail and name.  Checking unsuccessful server response")
    public void createUserWithoutPasswordTest() {
        user.setEmail(email);
        user.setName(name);
        Response response = userSteps.sendPostRequestApiAuthRegister(user);
        userSteps.checkFailedResponseApiAuthRegister(response);
    }

    @Test
    @DisplayName("Registering a user without a name and E-mail")
    @Description("Registering a user without name and E-mail, but with a random password.  Checking for unsuccessful server response")
    public void createUserWithoutNameAndEmailTest() {
        user.setPassword(password);
        Response response = userSteps.sendPostRequestApiAuthRegister(user);
        userSteps.checkFailedResponseApiAuthRegister(response);
    }

    @Test
    @DisplayName("Registering a user without a username and password")
    @Description("User registration without username and password, but with a random E-mail.  Checking unsuccessful server response")
    public void createUserWithoutNameAndPasswordTest() {
        user.setEmail(email);
        Response response = userSteps.sendPostRequestApiAuthRegister(user);
        userSteps.checkFailedResponseApiAuthRegister(response);
    }

    @Test
    @DisplayName("User registration without E-mail and password")
    @Description("User registration without E-mail and password, but with a random name.  Checking for unsuccessful server response")
    public void createUserWithoutEmailAndPasswordTest() {
        user.setName(name);
        Response response = userSteps.sendPostRequestApiAuthRegister(user);
        userSteps.checkFailedResponseApiAuthRegister(response);
    }

    @Test
    @DisplayName("Registering a user without all the data")
    @Description("Registering a user without all the data. Checking an unsuccessful server response")
    public void createUserWithoutAllTest() {
        Response response = userSteps.sendPostRequestApiAuthRegister(user);
        userSteps.checkFailedResponseApiAuthRegister(response);
    }

    @After
    @DisplayName("Deleting a user")
    @Description("Deleting a user with random data created")
    public void deleteRandomUser() {
        given().log().all()
                .header("Content-Type", "application/json")
                .body(new User(name, email, password))
                .delete("/api/auth/user");
    }
}
