
package pronosticos.unitarias;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pronosticos.CalculadoraPronosticos;

class AlertasTest {

    private final CalculadoraPronosticos calculadora = new CalculadoraPronosticos();

    @Test
    @DisplayName("Genera alerta cuando el error supera el umbral")
    void generaAlertaCuandoSuperaUmbral() {

        boolean resultado = calculadora.debeGenerarAlerta(12, 10);

        assertTrue(resultado);
    }

    @Test
    @DisplayName("Genera alerta cuando el error es igual al umbral")
    void generaAlertaCuandoIgualaUmbral() {

        boolean resultado = calculadora.debeGenerarAlerta(10, 10);

        assertTrue(resultado);
    }

    @Test
    @DisplayName("No genera alerta cuando el error es menor al umbral")
    void noGeneraAlertaCuandoEstaPorDebajo() {

        boolean resultado = calculadora.debeGenerarAlerta(7, 10);

        assertFalse(resultado);
    }

    @Test
    @DisplayName("Rechaza un umbral fuera del rango permitido")
    void rechazaUmbralFueraDeRango() {

        assertThrows(IllegalArgumentException.class,
                () -> calculadora.debeGenerarAlerta(12, 150));
    }
}
