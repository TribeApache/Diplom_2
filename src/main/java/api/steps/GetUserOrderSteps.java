package api.steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.hamcrest.Matchers;

import static io.restassured.RestAssured.given;

public class GetUserOrderSteps {

    private static final String ORDERS_URL = "/api/orders";

    @Step("Getting the order list of an authorized user")
    public Response getUserOrderWithAuthorization(String accessToken) {
        return given().log().all()
                .header("Content-Type", "application/json")
                .header("authorization", accessToken)
                .when()
                .get(ORDERS_URL);
    }

    @Step("Getting a list of orders without authorization")
    public Response getUserOrderWithoutAuthorization() {
        return given().log().all()
                .header("Content-Type", "application/json")
                .when()
                .get(ORDERS_URL);
    }

    @Step("Verifying successful response for getting the order list of an authorized user")
    public void verifySuccessfulResponseForUserOrderWithAuthorization(Response response) {
        response.then().log().all()
                .assertThat().body("success", Matchers.is(true))
                .and().body("orders", Matchers.notNullValue())
                .and().body("total", Matchers.any(Integer.class))
                .and().body("totalToday", Matchers.any(Integer.class))
                .and().statusCode(200);
    }

    @Step("Verifying unsuccessful response for getting a list of orders without authorization")
    public void verifyUnsuccessfulResponseForUserOrderWithoutAuthorization(Response response) {
        response.then().log().all()
                .assertThat().body("success", Matchers.is(false))
                .and().body("message", Matchers.is("You should be authorised"))
                .and().statusCode(401);
    }
}