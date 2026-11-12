
package pronosticos.bdd;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import pronosticos.CalculadoraPronosticos;

public class AlertasSteps {
    private final CalculadoraPronosticos calculadora = new CalculadoraPronosticos();
    private double umbral;
    private boolean alerta;
    // {int} captura el número de la frase y lo entrega como parámetro:
// así el mismo paso sirve para 10 %, 12 % o 7 %.
    @Dado("un umbral de alerta de {int} %")
    public void unUmbralDeAlertaDe(int valor) {
        umbral = valor;
    }
    @Cuando("el error del pronóstico es {int} %")
    public void elErrorDelPronosticoEs(int error) {
        alerta = calculadora.debeGenerarAlerta(error, umbral);
    }
    @Entonces("se genera una alerta")
    public void seGeneraUnaAlerta() {
        assertTrue(alerta);
    }
    @Entonces("no se genera una alerta")
    public void noSeGeneraUnaAlerta() {
        assertFalse(alerta);
    }
}
