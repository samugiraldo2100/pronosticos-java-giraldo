
package pronosticos.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pronosticos.CalculadoraPronosticos;

class MapeTest {

    private final CalculadoraPronosticos calculadora = new CalculadoraPronosticos();

    @Test
    @DisplayName("Calcula correctamente el MAPE con demandas normales")
    void calculaMapeCorrectamente() {

        List<Double> real = List.of(141.0, 138.0, 150.0);
        List<Double> pronostico = List.of(126.67, 133.67, 135.67);

        double resultado = calculadora.mape(real, pronostico);

        assertEquals(7.62, resultado, 0.01);
    }

    @Test
    @DisplayName("Excluye los periodos con demanda real igual a cero")
    void excluyePeriodosConDemandaCero() {

        List<Double> real = List.of(80.0, 0.0, 95.0);
        List<Double> pronostico = List.of(80.0, 0.0, 100.0);

        double resultado = calculadora.mape(real, pronostico);

        assertEquals(2.63, resultado, 0.01);
    }

    @Test
    @DisplayName("Lanza una excepcion cuando todas las demandas son cero")
    void rechazaSeriesSinPeriodosValidos() {

        List<Double> real = List.of(0.0, 0.0);
        List<Double> pronostico = List.of(10.0, 20.0);

        assertThrows(IllegalArgumentException.class,
                () -> calculadora.mape(real, pronostico));
    }
}
