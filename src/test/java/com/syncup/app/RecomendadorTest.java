package com.syncup.app;

import com.syncup.app.logic.*;
import com.syncup.app.model.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class RecomendadorTest {

    @Test
    void testRecomendadorFuncionamiento() {
        BibliotecaMusical b = new BibliotecaMusical();
        b.agregarCancion(new Cancion("RockStar", "Post", "Rock"));

        FavoritosManager fm = new FavoritosManager();
        fm.toggleFavorito("juan", new Cancion("RockStar", "Post", "Rock"));

        Recomendador r = new Recomendador(b, fm);
        var lista = r.recomendarParaUsuario("juan", 5);

        assertFalse(lista.isEmpty());
    }
}

