import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Avis } from '../avis.modeles';
import { SocleAvis } from '../avis.socle';
import { attendre, AVIS_EXEMPLE, requis, saisir, SocleFactice } from '../avis.testing';
import { DialogueAvisComponent } from './dialogue-avis.component';

function monter(avis: Avis | null = null) {
  const fermetures: unknown[] = [];
  TestBed.configureTestingModule({
    imports: [DialogueAvisComponent],
    providers: [
      provideHttpClient(),
      provideHttpClientTesting(),
      { provide: SocleAvis, useClass: SocleFactice },
      { provide: MAT_DIALOG_DATA, useValue: { idBrique: 5, avis } },
      { provide: MatDialogRef, useValue: { close: (v?: unknown) => fermetures.push(v) } },
    ],
  });
  const fixture = TestBed.createComponent(DialogueAvisComponent);
  fixture.detectChanges();
  const racine = fixture.nativeElement as HTMLElement;
  return {
    fixture,
    racine,
    fermetures,
    http: TestBed.inject(HttpTestingController),
    socle: TestBed.inject(SocleAvis) as unknown as SocleFactice,
    etoile: (n: number) => racine.querySelectorAll<HTMLButtonElement>('button[role="radio"]')[n - 1].click(),
    envoyer: () => requis(racine.querySelector('form')).dispatchEvent(new Event('submit')),
  };
}

describe('DialogueAvisComponent', () => {
  it('RG8 : sans note, rien n’est envoyé, l’erreur s’affiche et le focus va sur la note', async () => {
    const { fixture, racine, http, envoyer } = monter();

    envoyer();
    fixture.detectChanges();
    await attendre();

    http.expectNone('/api/briques/5/avis');
    expect(racine.textContent).toContain('Choisissez une note de 1 à 5.');
    expect(document.activeElement?.getAttribute('role')).toBe('radio');
  });

  it('RG3 : le compteur suit la saisie et plus de 2 000 caractères est refusé', () => {
    const { fixture, racine, http, etoile, envoyer } = monter();
    etoile(3);

    saisir(racine, 'textarea', 'a'.repeat(2001));
    fixture.detectChanges();
    expect(racine.textContent).toContain('2001 / 2 000');
    expect(racine.textContent).toContain('Au plus 2 000 caractères.');

    envoyer();
    http.expectNone('/api/briques/5/avis');
  });

  it('RG8 : un avis valide est envoyé nettoyé et le dialogue se ferme avec le résultat', () => {
    const { racine, http, fermetures, etoile, envoyer } = monter();
    etoile(4);
    saisir(racine, 'textarea', '  Très utile  ');

    envoyer();

    const requete = http.expectOne('/api/briques/5/avis');
    expect(requete.request.body).toEqual({ note: 4, commentaire: 'Très utile' });
    requete.flush(AVIS_EXEMPLE, { status: 201, statusText: 'Created' });
    expect(fermetures).toEqual([{ cree: true, valeur: AVIS_EXEMPLE }]);
  });

  it('RG8 : un avis existant pré-remplit la note et le commentaire', () => {
    const { racine } = monter({ ...AVIS_EXEMPLE, note: 2, commentaire: 'Moyen' });

    const coche = racine.querySelector('button[role="radio"][aria-checked="true"]');
    expect(coche?.getAttribute('aria-label')).toBe('2 sur 5');
    expect(requis(racine.querySelector<HTMLTextAreaElement>('textarea')).value).toBe('Moyen');
  });

  it('REQUETE_INVALIDE : le message du serveur s’affiche sous le champ concerné', () => {
    const { fixture, racine, http, fermetures, etoile, envoyer } = monter();
    etoile(5);
    envoyer();

    http.expectOne('/api/briques/5/avis').flush(
      { code: 'REQUETE_INVALIDE', message: 'Requête invalide.', details: [{ champ: 'commentaire', message: 'Commentaire refusé.' }] },
      { status: 400, statusText: 'Bad Request' },
    );
    fixture.detectChanges();

    expect(racine.textContent).toContain('Commentaire refusé.');
    expect(fermetures).toEqual([]);
  });

  it('RG15 : LIMITE_QUOTIDIENNE s’affiche en notification et le dialogue reste ouvert', () => {
    const { fixture, racine, http, fermetures, socle, etoile, envoyer } = monter();
    etoile(5);
    envoyer();

    http.expectOne('/api/briques/5/avis').flush(
      { code: 'LIMITE_QUOTIDIENNE', message: 'Limite quotidienne atteinte.' },
      { status: 429, statusText: 'Too Many Requests' },
    );
    fixture.detectChanges();

    expect(socle.messagesErreur).toEqual(['Limite quotidienne atteinte.']);
    expect(fermetures).toEqual([]);
    expect(requis(racine.querySelector<HTMLButtonElement>('button[type="submit"]')).disabled).toBe(false);
  });

  it('AUTO_EVALUATION_INTERDITE : notification puis fermeture', () => {
    const { http, fermetures, socle, etoile, envoyer } = monter();
    etoile(5);
    envoyer();

    http.expectOne('/api/briques/5/avis').flush(
      { code: 'AUTO_EVALUATION_INTERDITE', message: 'Vous ne pouvez pas évaluer votre propre brique.' },
      { status: 403, statusText: 'Forbidden' },
    );

    expect(socle.messagesErreur).toHaveLength(1);
    expect(fermetures).toHaveLength(1);
  });

  it('NON_AUTHENTIFIE : fermeture puis redirection vers la connexion', () => {
    const { http, fermetures, socle, etoile, envoyer } = monter();
    etoile(5);
    envoyer();

    http.expectOne('/api/briques/5/avis').flush(
      { code: 'NON_AUTHENTIFIE', message: 'Connexion requise.' },
      { status: 401, statusText: 'Unauthorized' },
    );

    expect(socle.connexions).toBe(1);
    expect(fermetures).toHaveLength(1);
  });
});
