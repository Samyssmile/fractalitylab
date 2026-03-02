package de.fractalitylab.generators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Complex number arithmetic")
class ComplexTest {

	private static final double EPSILON = 1e-10;

	@Test
	@DisplayName("add() sums real and imaginary parts")
	void addSumsComponents() {
		var a = new Complex(1, 2);
		var b = new Complex(3, 4);
		var result = a.add(b);

		assertThat(result.re()).isCloseTo(4, within(EPSILON));
		assertThat(result.im()).isCloseTo(6, within(EPSILON));
	}

	@Test
	@DisplayName("subtract() subtracts components")
	void subtractSubtractsComponents() {
		var a = new Complex(5, 3);
		var b = new Complex(2, 1);
		var result = a.subtract(b);

		assertThat(result.re()).isCloseTo(3, within(EPSILON));
		assertThat(result.im()).isCloseTo(2, within(EPSILON));
	}

	@Test
	@DisplayName("multiply() follows (a+bi)(c+di) = (ac-bd) + (ad+bc)i")
	void multiplyFollowsFormula() {
		var a = new Complex(1, 2);
		var b = new Complex(3, 4);
		var result = a.multiply(b);

		assertThat(result.re()).isCloseTo(-5, within(EPSILON));
		assertThat(result.im()).isCloseTo(10, within(EPSILON));
	}

	@Test
	@DisplayName("divide() computes z1/z2 correctly")
	void divideComputesCorrectly() {
		var a = new Complex(1, 2);
		var b = new Complex(3, 4);
		var result = a.divide(b);

		assertThat(result.re()).isCloseTo(11.0 / 25, within(EPSILON));
		assertThat(result.im()).isCloseTo(2.0 / 25, within(EPSILON));
	}

	@Test
	@DisplayName("pow(0) returns 1+0i")
	void powZeroReturnsOne() {
		var z = new Complex(3, 4);
		var result = z.pow(0);

		assertThat(result.re()).isCloseTo(1, within(EPSILON));
		assertThat(result.im()).isCloseTo(0, within(EPSILON));
	}

	@Test
	@DisplayName("pow(1) returns the original complex number")
	void powOneReturnsSelf() {
		var z = new Complex(3, 4);
		var result = z.pow(1);

		assertThat(result.re()).isCloseTo(3, within(EPSILON));
		assertThat(result.im()).isCloseTo(4, within(EPSILON));
	}

	@Test
	@DisplayName("pow(2) returns z*z")
	void powTwoReturnsSquare() {
		var z = new Complex(1, 1);
		var result = z.pow(2);

		assertThat(result.re()).isCloseTo(0, within(EPSILON));
		assertThat(result.im()).isCloseTo(2, within(EPSILON));
	}

	@Test
	@DisplayName("pow(3) returns z*z*z")
	void powThreeReturnsCube() {
		var z = new Complex(1, 1);
		var result = z.pow(3);

		assertThat(result.re()).isCloseTo(-2, within(EPSILON));
		assertThat(result.im()).isCloseTo(2, within(EPSILON));
	}

	@Test
	@DisplayName("pow() with negative exponent throws IllegalArgumentException")
	void powNegativeThrows() {
		var z = new Complex(1, 1);

		assertThatThrownBy(() -> z.pow(-1))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Negative");
	}

	@Test
	@DisplayName("modulus() computes |z| correctly")
	void modulusComputesCorrectly() {
		var z = new Complex(3, 4);

		assertThat(z.modulus()).isCloseTo(5, within(EPSILON));
	}

	@Test
	@DisplayName("modulus() of zero is zero")
	void modulusOfZeroIsZero() {
		assertThat(new Complex(0, 0).modulus()).isCloseTo(0, within(EPSILON));
	}

	@Test
	@DisplayName("conjugate() negates imaginary part")
	void conjugateNegatesImaginary() {
		var z = new Complex(3, 4);
		var result = z.conjugate();

		assertThat(result.re()).isCloseTo(3, within(EPSILON));
		assertThat(result.im()).isCloseTo(-4, within(EPSILON));
	}
}
