package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.aws.GestorObjetosS3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/s3")
public class S3Controller {

    @Autowired
    private GestorObjetosS3 gestorObjetosS3;

    @GetMapping("/url-subida")
    public ResponseEntity<Map<String, String>> obtenerUrlSubida(@RequestParam("nombreArchivo") String nombreArchivo,
                                                                @RequestParam(value = "tipo", defaultValue = "image/jpeg") String tipo) {
        String clave = "perfiles/" + nombreArchivo;
        String urlFirma = gestorObjetosS3.obtenerURLPutDocumentoEnS3(clave, tipo);

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("url", urlFirma);
        respuesta.put("clave", clave);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/url-lectura")
    public ResponseEntity<Map<String, String>> obtenerUrlLectura(@RequestParam("clave") String clave) {
        String urlFirma = gestorObjetosS3.obtenerURLGetDocumentoEnS3(clave);

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("url", urlFirma);

        return ResponseEntity.ok(respuesta);
    }
}