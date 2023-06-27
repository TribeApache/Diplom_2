package api.steps;

import api.model.Ingredients;
import api.model.Order;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import org.hamcrest.Matchers;

import static io.restassured.RestAssured.given;

public class OrderSteps {

    private static final String INGREDIENTS_URL = "/api/ingredients";
    private static final String ORDERS_URL = "/api/orders";

    @Step("Getting a list of ingredients (objects). GET request to the " + INGREDIENTS_URL + " handle")
    public Ingredients getIngredients() {
        return given()
                .header("Content-Type", "application/json")
                .log().all()
                .get(INGREDIENTS_URL)
                .body()
                .as(Ingredients.class);
    }

    @Step("Creating an order with authorization. POST request to " + ORDERS_URL + " handle")
    public Response createOrderWithAuthorization(Order order, String token) {
        return given().log().all().filter(new AllureRestAssured())
                .header("Content-Type", "application/json")
                .header("authorization", token)
                .body(order)
                .when()
                .post(ORDERS_URL);
    }

    @Step("Creating an order without authorization. POST request for " + ORDERS_URL + " handle")
    public Response createOrderWithoutAuthorization(Order order) {
        return given().log().all()
                .filter(new AllureRestAssured())
                .header("Content-Type", "application/json")
                .body(order)
                .when()
                .post(ORDERS_URL);
    }

    @Step("Unsuccessful server response to create an order without ingredients")
    public void checkFailedResponseApiOrders(Response response) {
        response.then().log().all()
                .assertThat().body("success", Matchers.is(false))
                .and().body("message", Matchers.is("Ingredient ids must be provided"))
                .and().statusCode(400);
    }

    @Step("Getting the order list of an authorized user")
    public Response getUserOrderWithAuthorization(String accessToken) {
        GetUserOrderSteps getUserOrderSteps = new GetUserOrderSteps();
        return getUserOrderSteps.getUserOrderWithAuthorization(accessToken);
    }

    @Step("Getting a list of orders without authorization")
    public Response getUserOrderWithoutAuthorization() {
        GetUserOrderSteps getUserOrderSteps = new GetUserOrderSteps();
        return getUserOrderSteps.getUserOrderWithoutAuthorization();
    }

    @Step("Verifying successful response for getting the order list of an authorized user")
    public void verifySuccessfulResponseForUserOrderWithAuthorization(Response response) {
        GetUserOrderSteps getUserOrderSteps = new GetUserOrderSteps();
        getUserOrderSteps.verifySuccessfulResponseForUserOrderWithAuthorization(response);
    }

    @Step("Verifying unsuccessful response for getting a list of orders without authorization")
    public void verifyUnsuccessfulResponseForUserOrderWithoutAuthorization(Response response) {
        GetUserOrderSteps getUserOrderSteps = new GetUserOrderSteps();
        getUserOrderSteps.verifyUnsuccessfulResponseForUserOrderWithoutAuthorization(response);
    }
}