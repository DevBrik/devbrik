import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Test } from '../avis.modeles';
import { SocleAvis } from '../avis.socle';
import { attendre, requis, saisir, SocleFactice } from '../avis.testing';
import { DialogueTestComponent } from './dialogue-test.component';

const TEST_EXEMPLE: Test = {
  id: 3, resultat: 'KO', environnement: 'Linux', versionCode: 1,
  auteur: null, dateTest: '2026-11-05T14:30:00Z',
};

function monter(test: Test | null = null, versionCode = 2) {
  const fermetures: unknown[] = [];
  TestBed.configureTestingModule({
    imports: [DialogueTestComponent],
    providers: [
      provideHttpClient(),
      provideHttpClientTesting(),
      { provide: SocleAvis, useClass: SocleFactice },
      { provide: MAT_DIALOG_DATA, useValue: { idBrique: 7, versionCode, test } },
      { provide: MatDialogRef, useValue: { close: (v?: unknown) => fermetures.push(v) } },
    ],
  });
  const fixture = TestBed.createComponent(DialogueTestComponent);
  fixture.detectChanges();
  const racine = fixture.nativeElement as HTMLElement;
  return {
    fixture,
    racine,
    fermetures,
    http: TestBed.inject(HttpTestingController),
    choisir: (valeur: string) =>
      requis(racine.querySelector<HTMLInputElement>(`input[type="radio"][value="${valeur}"]`)).click(),
    envoyer: () => requis(racine.querySelector('form')).dispatchEvent(new Event('submit')),
  };
}

describe('DialogueTestComponent', () => {
  it('RG9 : sans résultat, rien n’est envoyé et le focus va sur un bouton radio', async () => {
    const { fixture, racine, http, envoyer } = monter();

    envoyer();
    fixture.detectChanges();
    await attendre();

    http.expectNone('/api/briques/7/test');
    expect(racine.textContent).toContain('Indiquez si la brique fonctionne ou non.');
    expect(document.activeElement?.getAttribute('type')).toBe('radio');
  });

  it('RG9 : le test est enregistré pour la version actuelle avec un environnement nettoyé', () => {
    const { racine, http, fermetures, choisir, envoyer } = monter();
    expect(racine.textContent).toContain('version 2 du code');
    choisir('OK');
    saisir(racine, '#test-environnement', '  Java 21  ');

    envoyer();

    const requete = http.expectOne('/api/briques/7/test');
    expect(requete.request.body).toEqual({ resultat: 'OK', environnement: 'Java 21' });
    requete.flush({ ...TEST_EXEMPLE, resultat: 'OK', versionCode: 2 }, { status: 201, statusText: 'Created' });
    expect(fermetures).toHaveLength(1);
  });

  it('RG9 : l’environnement est facultatif', () => {
    const { http, choisir, envoyer } = monter();
    choisir('KO');

    envoyer();

    expect(http.expectOne('/api/briques/7/test').request.body).toEqual({ resultat: 'KO' });
  });

  it('RG3 : un environnement de 121 caractères est refusé, le compteur est affiché', () => {
    const { fixture, racine, http, choisir, envoyer } = monter();
    choisir('OK');
    saisir(racine, '#test-environnement', 'e'.repeat(121));
    fixture.detectChanges();

    envoyer();

    http.expectNone('/api/briques/7/test');
    expect(racine.textContent).toContain('121 / 120');
    expect(racine.textContent).toContain('Au plus 120 caractères.');
  });

  it('RG9 : un test d’une ancienne version est signalé comme remplacé', () => {
    const { racine } = monter(TEST_EXEMPLE, 2);

    expect(racine.textContent).toContain('porte sur la version 1');
    expect(requis(racine.querySelector<HTMLInputElement>('input[value="KO"]')).checked).toBe(true);
  });
});
