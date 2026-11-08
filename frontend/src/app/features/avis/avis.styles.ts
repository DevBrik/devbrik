/** Styles communs. Couleurs uniquement par variables de la charte (jamais #EA580C en dur). */
export const STYLES_COMMUNS = `
  .btn {
    min-height: 48px; min-width: 44px; padding: 0 1.25rem; border: 2px solid transparent;
    border-radius: 0.5rem; font: inherit; font-size: 1rem; font-weight: 600; color: #fff; cursor: pointer;
  }
  .btn-primaire { background: var(--db-primaire); }
  .btn-accent { background: var(--db-orange-bouton); }
  .btn-destructif { background: var(--db-erreur); }
  .btn-contour-erreur { background: transparent; color: var(--db-erreur); border-color: var(--db-erreur); }
  .btn:disabled { opacity: 0.55; cursor: not-allowed; }
  :focus-visible { outline: 3px solid var(--db-focus); outline-offset: 2px; }

  .champ { display: grid; gap: 0.375rem; margin: 0 0 1rem; }
  fieldset.champ { border: 0; padding: 0; min-width: 0; }
  fieldset.champ legend { padding: 0; margin-bottom: 0.375rem; }
  .etiquette { font-size: 1rem; font-weight: 600; }
  .obligatoire { color: var(--db-orange-texte); }
  textarea, input[type='text'] {
    width: 100%; box-sizing: border-box; padding: 0.75rem; font: inherit; font-size: 1rem;
    border: 1px solid var(--db-bordure); border-radius: 0.5rem;
  }
  .compteur, .aide { margin: 0; font-size: 0.875rem; }
  .erreur { margin: 0; font-size: 1rem; color: var(--db-erreur); }
  .objet { margin: 0 0 1rem; }
  .info { margin: 0 0 1rem; padding: 0.5rem 0.75rem; border-left: 4px solid var(--db-orange); }

  .option { display: flex; align-items: center; gap: 0.625rem; min-height: 44px; cursor: pointer; }
  .option input[type='radio'] { width: 1.5rem; height: 1.5rem; margin: 0; accent-color: var(--db-primaire); }
  .pastille {
    display: inline-flex; align-items: center; gap: 0.375rem; padding: 0.25rem 0.75rem;
    border-radius: 1rem; font-weight: 600;
  }
  .pastille.ok { color: var(--db-succes); background: color-mix(in srgb, var(--db-succes) 15%, white); }
  .pastille.ko { color: var(--db-erreur); background: color-mix(in srgb, var(--db-erreur) 15%, white); }

  .actions { display: flex; flex-wrap: wrap; gap: 0.75rem; }
  @media (max-width: 480px) {
    .actions { flex-direction: column-reverse; }
    .actions .btn { width: 100%; }
  }
  .sr-only {
    position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden;
    clip: rect(0, 0, 0, 0); white-space: nowrap; border: 0;
  }
   `;
