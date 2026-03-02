package de.fractalitylab.generators;

/**
 * Immutable complex number for fractal computations.
 */
public record Complex(double re, double im) {

	public Complex add(Complex b) {
		return new Complex(re + b.re, im + b.im);
	}

	public Complex subtract(Complex b) {
		return new Complex(re - b.re, im - b.im);
	}

	public Complex multiply(Complex b) {
		return new Complex(re * b.re - im * b.im, re * b.im + im * b.re);
	}

	public Complex divide(Complex b) {
		double denom = b.re * b.re + b.im * b.im;
		Complex conjugate = b.conjugate();
		Complex numerator = this.multiply(conjugate);
		return new Complex(numerator.re / denom, numerator.im / denom);
	}

	/**
	 * Raises this complex number to an integer power using exponentiation by squaring (O(log n)).
	 *
	 * @param power non-negative integer exponent
	 * @return this^power
	 * @throws IllegalArgumentException if power is negative
	 */
	public Complex pow(int power) {
		if (power < 0) {
			throw new IllegalArgumentException("Negative exponent not supported: " + power);
		}
		if (power == 0) {
			return new Complex(1, 0);
		}
		if (power == 1) {
			return this;
		}

		Complex result = new Complex(1, 0);
		Complex base = this;
		int exp = power;
		while (exp > 0) {
			if ((exp & 1) == 1) {
				result = result.multiply(base);
			}
			base = base.multiply(base);
			exp >>= 1;
		}
		return result;
	}

	public double modulus() {
		return Math.sqrt(re * re + im * im);
	}

	public Complex conjugate() {
		return new Complex(re, -im);
	}
}
