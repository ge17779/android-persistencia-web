package br.com.alura.estoque.repository;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;
import retrofit2.Response;

public class ProdutoRepository {

    private final ProdutoDAO dao;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
    }

    public void buscaProdutos(ProdutosCarregadosListener listener) {
        buscaProdutosInterno(listener);
    }

    private void buscaProdutosInterno(ProdutosCarregadosListener listener) {
        new BaseAsyncTask<>(dao::buscaTodos, new BaseAsyncTask.FinalizadaListener<List<Produto>>() {
            @Override
            public void quandoFinalizada(List<Produto> resultado) {
                listener.quandoCarregados(resultado);
                buscaProdutosNaApi();
            }
        }
//                resultado -> {
//                    listener.quandoCarregados(resultado);
//                    buscaProdutosNaApi();
//                }
        )
                .execute();
    }

    private void buscaProdutosNaApi() {
        ProdutoService service = new EstoqueRetrofit().getProdutoService();
        Call<List<Produto>> call = service.buscaTodos();
        new BaseAsyncTask<>(() -> {
            try {
                Response<List<Produto>> resposta = call.execute();
                List<Produto> produtosNovos = resposta.body();
                dao.salva(produtosNovos);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dao.buscaTodos();
        }, produtosNovos -> {
            //notifica que o dado está pronto
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface ProdutosCarregadosListener{
        void quandoCarregados(List<Produto> produtos);
    }

}
