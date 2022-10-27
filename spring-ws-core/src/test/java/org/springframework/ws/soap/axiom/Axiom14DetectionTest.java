package org.springframework.ws.soap.axiom;

import org.junit.jupiter.api.Test;
import org.springframework.ws.soap.axiom.support.AxiomUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class Axiom14DetectionTest {

    @Test
    void detectedAxiom14Correctly() {
        assertThat(AxiomUtils.AXIOM14_IS_PRESENT()).isTrue();
    }
}
