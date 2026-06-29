import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiTests {

    private static final int unexistingPetId = 54532323;

    @BeforeAll
    public static void clearTestData() {
        given()
                .when()
                .delete("https://petstore.swagger.io/v2/pet/" + unexistingPetId)
                .then()
                .log().all();
    }

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
    public void petNotFoundTestBdd() {
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

    @Test
    @DisplayName("Добавление нового питомца")
    public void newPetTest() {
        Integer id = unexistingPetId;
        String name = "cat";
        String status = "available";

        Map<String, String> request = new HashMap<>();
        request.put("id", id.toString());
        request.put("name", name);
        request.put("status", status);

        given().contentType("application/json")
                .body(request)
                .when()
                .post(baseURI + "pet/")
                .then()
                .log().all()
                .time(lessThan(5000L))
                .assertThat()
                .statusCode(200)
                .body("id", equalTo(id), "name", equalTo(name), "status", equalTo(status));

    }

    @Test
    @DisplayName("Поиск питомца по статусу available")
    public void searchPetWithStatusAvailable() {
        given().when()
                .get(baseURI + "pet/" + "findByStatus?status=available")
                .then()
                .log().all()
                .statusCode(200)
                .time(lessThan(3000L))
                .body("status", everyItem(equalTo("available")), "id", everyItem(notNullValue()
                ), "name", everyItem(notNullValue()));
    }
}
