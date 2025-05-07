package servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositorios.RegistroRepositorio;
import repositorios.UsuarioRepositorio;
import repositorios.EmpresaRepositorio;
import modelos.Empresa;
import modelos.Usuario;

import java.util.List;

@Service
public class EmpresaServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final RegistroRepositorio registroRepositorio;
    private final EmpresaRepositorio empresaRepositorio;

    @Autowired
    public EmpresaServicio(UsuarioRepositorio usuarioRepositorio, RegistroRepositorio registroRepositorio, EmpresaRepositorio empresaRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.registroRepositorio = registroRepositorio;
        this.empresaRepositorio = empresaRepositorio;
    }

    @Transactional
    public void eliminar(Empresa empresa) {
        List<Usuario> usuarios = usuarioRepositorio.findByEmpresaId(empresa.getId());

        for (Usuario usuario : usuarios) {
            registroRepositorio.deleteByUsuario_Id(usuario.getId());
        }

        for (Usuario usuario : usuarios) {
            usuarioRepositorio.deleteById(usuario.getId());
        }

        empresaRepositorio.deleteById(empresa.getId());
    }
}
