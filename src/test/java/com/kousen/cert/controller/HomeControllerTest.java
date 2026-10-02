package com.kousen.cert.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HomeControllerTest {

    @Test
    void shouldRedirectRootToIndexPage() {
        var redirect = new HomeController().home();

        assertThat(redirect.getUrl()).isEqualTo("/index.html");
    }
}
