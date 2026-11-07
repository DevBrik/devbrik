import { HttpErrorResponse } from '@angular/common/http';
import { FormControl } from '@angular/forms';
import { lireErreur, messageChamp } from './avis.erreurs';

describe('lireErreur', () => {
  it('reprend code, message et détails du format 11.2', () => {
    const erreur = lireErreur(
      new HttpErrorResponse({
        status: 400,
        error: { code: 'REQUETE_INVALIDE', message: 'Note invalide.', details: [{ champ: 'note', message: 'Entre 1 et 5' }] },
      }),
    );
    expect(erreur.code).toBe('REQUETE_INVALIDE');
    expect(erreur.details).toEqual([{ champ: 'note', message: 'Entre 1 et 5' }]);
  });

  it('signale un serveur injoignable', () => {
    expect(lireErreur(new HttpErrorResponse({ status: 0 })).code).toBe('RESEAU');
  });

  it('renvoie un message générique pour une erreur inconnue', () => {
    expect(lireErreur(new Error('x')).message).toContain('Une erreur est survenue');
  });
});

describe('messageChamp', () => {
  it('n’affiche rien tant que le champ n’a pas été touché', () => {
    const champ = new FormControl('a'.repeat(5));
    champ.setErrors({ maxlength: true });
    expect(messageChamp(champ, { maxlength: 'Trop long.' })).toBeNull();
  });

  it('préfère le message du serveur', () => {
    const champ = new FormControl('x');
    champ.markAsTouched();
    champ.setErrors({ serveur: 'Message serveur.', maxlength: true });
    expect(messageChamp(champ, { maxlength: 'Trop long.' })).toBe('Message serveur.');
  });
});
