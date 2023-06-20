package api.steps;

import api.model.User;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.hamcrest.Matchers;

import java.util.Locale;

import static io.restassured.RestAssured.given;

public class UserSteps {

    private final static String ERROR_MESSAGE_TEXT_REGISTER = "Email, password and name are required fields";
    private final static String ERROR_MESSAGE_TEXT_LOGIN = "email or password are incorrect";
    private final static String ERROR_MESSAGE_TEXT_USER = "You should be authorised";

    @Step("User registration. POST request to /api/auth/register handle")
    public Response sendPostRequestApiAuthRegister(User user) {
        return given().log().all()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post("/api/auth/register");
    }

    @Step("Failed server response to user registration attempt")
    public void checkFailedResponseApiAuthRegister(Response response) {
        response.then().log().all()
                .assertThat().body("success", Matchers.is(false))
                .and().body("message", Matchers.is(ERROR_MESSAGE_TEXT_REGISTER))
                .and().statusCode(403);
    }


    @Step("User authorization. POST request to /api/auth/login")
    public Response sendPostRequestApiAuthLogin(User user) {
        return given()
                .log()
                .all()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post("/api/auth/login");
    }

    @Step("Failed server response to user authorization attempt")
    public void checkFailedResponseApiAuthLogin(Response response) {
        response.then().log().all()
                .assertThat().body("success", Matchers.is(false))
                .and().body("message", Matchers.is(ERROR_MESSAGE_TEXT_LOGIN))
                .and().statusCode(401);
    }

    @Step("Changing user data with authorization. PATCH request to the /api/auth/user handle")
    public Response sendPatchRequestWithAuthorizationApiAuthUser(User user, String token) {
        return given()
                .log()
                .all()
                .header("Content-Type", "application/json")
                .header("authorization", token)
                .body(user)
                .when()
                .patch("/api/auth/user");
    }

    @Step("Changing user data without authorization. PATCH request to /api/auth/user handle")
    public Response sendPatchRequestWithoutAuthorizationApiAuthUser(User user) {
        return given()
                .log()
                .all()
                .header("Content-Type", "application/json")
                .body(user)
                .when()
                .patch("/api/auth/user");
    }

    @Step("Successful server response to user data change")
    public void checkSuccessResponseApiAuthUser(Response response, String email, String name) {
        response.then().log().all()
                .assertThat()
                .body("success", Matchers.is(true))
                .and().body("user.email", Matchers.is(email.toLowerCase(Locale.ROOT)))
                .and().body("user.name", Matchers.is(name))
                .and().statusCode(200);
    }

    @Step("Unsuccessful server response to user data change")
    public void checkFailedResponseApiAuthUser(Response response) {
        response.then().log().all()
                .assertThat().body("success", Matchers.is(false))
                .and().body("message", Matchers.is(ERROR_MESSAGE_TEXT_USER))
                .and().statusCode(401);
    }
}