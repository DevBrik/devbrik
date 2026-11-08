import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { SocleAvis } from '../avis.socle';
import { attendre, requis, saisir, SocleFactice } from '../avis.testing';
import { DialogueSignalementComponent } from './dialogue-signalement.component';

function monter() {
  const fermetures: unknown[] = [];
  TestBed.configureTestingModule({
    imports: [DialogueSignalementComponent],
    providers: [
      provideHttpClient(),
      provideHttpClientTesting(),
      { provide: SocleAvis, useClass: SocleFactice },
      { provide: MAT_DIALOG_DATA, useValue: { idBrique: 9 } },
      { provide: MatDialogRef, useValue: { close: (v?: unknown) => fermetures.push(v) } },
    ],
  });
  const fixture = TestBed.createComponent(DialogueSignalementComponent);
  fixture.detectChanges();
  const racine = fixture.nativeElement as HTMLElement;
  return {
    fixture,
    racine,
    fermetures,
    http: TestBed.inject(HttpTestingController),
    socle: TestBed.inject(SocleAvis) as unknown as SocleFactice,
    choisir: (valeur: string) =>
      requis(racine.querySelector<HTMLInputElement>(`input[type="radio"][value="${valeur}"]`)).click(),
    envoyer: () => requis(racine.querySelector('form')).dispatchEvent(new Event('submit')),
  };
}

describe('DialogueSignalementComponent', () => {
  it('RG11 : les six motifs sont proposés, dont « Contenu inapproprié »', () => {
    const { racine } = monter();

    const libelles = Array.from(racine.querySelectorAll('label.option')).map((l) => l.textContent?.trim());
    expect(libelles).toEqual([
      'Contenu inapproprié', 'Code dangereux', 'Violation de droits d’auteur',
      'Informations incorrectes', 'Spam ou publicité', 'Autre',
    ]);
  });

  it('RG11 : sans motif, rien n’est envoyé et le focus va sur un bouton radio', async () => {
    const { fixture, racine, http, envoyer } = monter();

    envoyer();
    fixture.detectChanges();
    await attendre();

    http.expectNone('/api/briques/9/signalements');
    expect(racine.textContent).toContain('Choisissez un motif.');
    expect(document.activeElement?.getAttribute('type')).toBe('radio');
  });

  it('RG11 : INAPPROPRIE et la description nettoyée sont envoyés', () => {
    const { racine, http, fermetures, choisir, envoyer } = monter();
    choisir('INAPPROPRIE');
    saisir(racine, 'textarea', '  Contenu choquant  ');

    envoyer();

    const requete = http.expectOne('/api/briques/9/signalements');
    expect(requete.request.body).toEqual({ motif: 'INAPPROPRIE', details: 'Contenu choquant' });
    requete.flush({ id: 1 }, { status: 201, statusText: 'Created' });
    expect(fermetures).toEqual([true]);
  });

  it('RG3 : une description de 1001 caractères est refusée', () => {
    const { fixture, racine, http, choisir, envoyer } = monter();
    choisir('SPAM');
    saisir(racine, 'textarea', 'd'.repeat(1001));
    fixture.detectChanges();

    envoyer();

    http.expectNone('/api/briques/9/signalements');
    expect(racine.textContent).toContain('Au plus 1 000 caractères.');
  });

  it('RG11 : un deuxième signalement donne une notification puis ferme le dialogue', () => {
    const { http, fermetures, socle, choisir, envoyer } = monter();
    choisir('AUTRE');
    envoyer();

    http.expectOne('/api/briques/9/signalements').flush(
      { code: 'SIGNALEMENT_DEJA_ENVOYE', message: 'Vous avez déjà signalé cette brique.' },
      { status: 409, statusText: 'Conflict' },
    );

    expect(socle.messagesErreur).toEqual(['Vous avez déjà signalé cette brique.']);
    expect(fermetures).toHaveLength(1);
  });
});
