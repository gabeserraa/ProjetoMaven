/* Central de Chamados — melhorias progressivas sobre as páginas renderizadas pelo Thymeleaf.
   Sem este arquivo tudo continua funcionando; ele só adiciona confirmação, atalhos e máscaras. */
(() => {
  'use strict';
  const $ = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => [...r.querySelectorAll(s)];
  const scrim = $('#scrim');

  // Menu lateral no celular
  $('#menuBtn')?.addEventListener('click', () => { $('#sidebar').classList.add('open'); scrim.hidden = false; });
  scrim?.addEventListener('click', () => { $('#sidebar').classList.remove('open'); if ($('#dialog').hidden) scrim.hidden = true; });

  // Linhas de tabela clicáveis (o link do título continua sendo o caminho acessível)
  $$('tr[data-href]').forEach((tr) => tr.addEventListener('click', (e) => {
    if (e.target.closest('a, button, form, input, select')) return;
    window.location.href = tr.dataset.href;
  }));

  // Filtros que se aplicam ao mudar; campos vazios não vão para a URL
  $$('select[data-autosubmit]').forEach((sel) => sel.addEventListener('change', () => sel.form.requestSubmit()));
  $$('form[method="get"]').forEach((f) => f.addEventListener('submit', () => {
    $$('select, input[type="search"]', f).forEach((el) => { if (!el.value) el.disabled = true; });
  }));

  // Confirmação antes de enviar formulários marcados com data-confirm
  const dialog = $('#dialog');
  let pendente = null;
  const fecharDialogo = () => { dialog.hidden = true; scrim.hidden = true; pendente = null; };
  $$('form[data-confirm]').forEach((form) => form.addEventListener('submit', (e) => {
    e.preventDefault();
    pendente = form;
    $('#dialogTitle').textContent = form.dataset.titulo || 'Confirmar?';
    $('#dialogText').textContent = form.dataset.texto || '';
    const ok = $('#dialogOk');
    ok.textContent = form.dataset.ok || 'Excluir';
    ok.className = `btn ${form.dataset.tipo === 'primary' ? 'primary' : 'danger'}`;
    dialog.hidden = false;
    scrim.hidden = false;
    ok.focus();
  }));
  $('#dialogOk')?.addEventListener('click', () => { const f = pendente; fecharDialogo(); f?.submit(); });
  $('#dialogCancel')?.addEventListener('click', fecharDialogo);
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && !dialog.hidden) fecharDialogo(); });

  // Contador de caracteres em textareas com maxlength
  $$('textarea[data-counter][maxlength]').forEach((t) => {
    const c = document.createElement('div');
    c.className = 'counter';
    t.after(c);
    const up = () => { c.textContent = `${t.value.length} / ${t.maxLength}`; };
    t.addEventListener('input', up);
    up();
  });

  // Máscara de telefone brasileiro
  $$('input[data-mask="telefone"]').forEach((input) => input.addEventListener('input', () => {
    const d = input.value.replace(/\D/g, '').slice(0, 11);
    if (!d) { input.value = ''; return; }
    if (d.length <= 2) input.value = `(${d}`;
    else if (d.length <= 6) input.value = `(${d.slice(0, 2)}) ${d.slice(2)}`;
    else if (d.length <= 10) input.value = `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`;
    else input.value = `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`;
  }));

  // Foco no primeiro campo com erro
  $('.field.invalid input, .field.invalid textarea, .field.invalid select')?.focus();

  // Mensagens de sucesso somem sozinhas; erros ficam mais tempo
  $$('.toast').forEach((t) => setTimeout(() => t.remove(), t.classList.contains('error') ? 8000 : 3500));
})();
