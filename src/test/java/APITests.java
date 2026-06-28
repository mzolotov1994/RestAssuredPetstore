import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class APITests {

    private final int unexistingPetId = 54532323;

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "https://petstore.swagger.io/v2/";
    }

    @Test //Not BDD
    public void notFoundTestWithAssert() {
        RestAssured.baseURI += "pet/" + unexistingPetId; //Добавили эндпоинт и id

        //Создаем объект requestSpecification
        requestSpecification = given();

        //Вызываем get метод, ответ кладем в response
        Response response = requestSpecification.get();

        //Выводим response на консоль
        System.out.println("Response: " + response.asPrettyString());

        assertEquals(404, response.statusCode(), "Не тот status code");
        assertEquals("HTTP/1.1 404 Not Found", response.statusLine(), "Некорректная status line");
        assertEquals("Pet not found", response.jsonPath().get("message"), "Не то сообщение об ошибке");

    }

    @Test //Not BDD
    public void petNotFoundTest() {
        RestAssured.baseURI += "pet/" + unexistingPetId; //Добавили эндпоинт и id

        //Создаем объект requestSpecification
        requestSpecification = given();

        //Вызываем get метод, ответ кладем в response
        Response response = requestSpecification.get();

        //Выводим response на консоль
        System.out.println("Response: " + response.asPrettyString());

        //Объект типа ValidatableResponse нужен для валидации ответа
        ValidatableResponse validatableResponse = response.then();

        //Проверяем status code
        validatableResponse.statusCode(404);

        //Проверяем status line
        validatableResponse.statusLine("HTTP/1.1 404 Not Found");

        //Проверяем response body
        validatableResponse.body("message", equalTo("Pet not found"));

    }

    @Test
    public void petNotFoundTest_BDD() {
        given().when()
                .get(baseURI + "pet/" + unexistingPetId)
                .then()
                .log().all()
                .statusCode(404)
                .statusLine("HTTP/1.1 404 Not Found")
                .body("message", equalTo("Pet not found"));
    }

    @Test
    public void checkTypeForPetTest() {
        given().when()
                .get(baseURI + "pet/" + unexistingPetId)
                .then()
                .log().all()
                .body("type", equalTo("error"));
    }

}
