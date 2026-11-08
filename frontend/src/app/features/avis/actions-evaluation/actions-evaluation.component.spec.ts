import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BriqueEvaluable } from '../avis.modeles';
import { SocleAvis } from '../avis.socle';
import { requis, SocleFactice } from '../avis.testing';
import { ActionsEvaluationComponent } from './actions-evaluation.component';

const BRIQUE: BriqueEvaluable = { id: 5, titre: 'Validation', statut: 'PUBLIEE', versionCode: 2, auteur: { id: 99 } };

function monter(brique: BriqueEvaluable = BRIQUE, idUtilisateur: number | null = 10) {
  TestBed.configureTestingModule({
    imports: [ActionsEvaluationComponent],
    providers: [provideHttpClient(), provideHttpClientTesting(), { provide: SocleAvis, useClass: SocleFactice }],
  });
  const socle = TestBed.inject(SocleAvis) as unknown as SocleFactice;
  socle.idUtilisateur.set(idUtilisateur);
  const fixture = TestBed.createComponent(ActionsEvaluationComponent);
  fixture.componentRef.setInput('brique', brique);
  fixture.detectChanges();
  const racine = fixture.nativeElement as HTMLElement;
  const bouton = (libelle: string) =>
    requis(Array.from(racine.querySelectorAll('button')).find((b) => b.textContent?.trim() === libelle));
  return { fixture, racine, socle, bouton, http: TestBed.inject(HttpTestingController) };
}

describe('ActionsEvaluationComponent', () => {
  it('RG8, RG9 : sur sa propre brique, Tester et Donner mon avis sont désactivés avec l’explication', () => {
    const { racine, bouton, http } = monter(BRIQUE, 99);

    expect(bouton('Tester cette brique').disabled).toBe(true);
    expect(bouton('Donner mon avis').disabled).toBe(true);
    expect(bouton('Signaler').disabled).toBe(false);
    expect(racine.textContent).toContain('Vous ne pouvez pas évaluer votre propre brique.');
    http.expectNone('/api/briques/5/moi');
  });

  it('un visiteur est renvoyé vers la connexion sans ouvrir de dialogue ni appeler /moi', () => {
    const { bouton, socle, http } = monter(BRIQUE, null);

    bouton('Tester cette brique').click();
    bouton('Donner mon avis').click();
    bouton('Signaler').click();

    expect(socle.connexions).toBe(3);
    http.expectNone('/api/briques/5/moi');
  });

  it('un développeur non auteur charge /moi pour pré-remplir son avis et son test', () => {
    const { fixture, racine, http } = monter();

    http.expectOne('/api/briques/5/moi').flush({
      monAvis: { id: 1, note: 4, commentaire: null, auteur: null, dateCreation: 'x', dateModification: 'x' },
      monTest: { id: 2, resultat: 'OK', environnement: 'Java 21', versionCode: 2, auteur: null, dateTest: 'x' },
    });
    fixture.detectChanges();

    expect(racine.textContent).toContain('Votre avis : 4 sur 5');
    expect(racine.textContent).toContain('Testé OK');
    expect(racine.textContent).not.toContain('Il ne compte plus');
  });

  it('RG9 : le code a changé, mon test de la version 1 ne compte plus et l’interface le dit', () => {
    const { fixture, racine, http } = monter({ ...BRIQUE, versionCode: 2 });

    http.expectOne('/api/briques/5/moi').flush({
      monAvis: null,
      monTest: { id: 2, resultat: 'OK', environnement: null, versionCode: 1, auteur: null, dateTest: 'x' },
    });
    fixture.detectChanges();

    expect(racine.textContent).toContain('Votre test porte sur la version 1');
    expect(racine.textContent).toContain('la version actuelle est la 2');
    expect(racine.textContent).toContain('Il ne compte plus');
  });

  it('RG7 : une brique masquée désactive l’évaluation et le signalement', () => {
    const { bouton, racine, http } = monter({ ...BRIQUE, statut: 'MASQUEE' });

    expect(bouton('Tester cette brique').disabled).toBe(true);
    expect(bouton('Signaler').disabled).toBe(true);
    expect(racine.textContent).toContain('Cette brique est masquée');
    http.expectNone('/api/briques/5/moi');
  });

  it('une erreur de chargement de /moi propose de réessayer', () => {
    const { fixture, racine, http } = monter();

    http.expectOne('/api/briques/5/moi').flush(
      { code: 'ERREUR_INTERNE', message: 'Erreur inattendue.' },
      { status: 500, statusText: 'Server Error' },
    );
    fixture.detectChanges();

    expect(racine.textContent).toContain('Erreur inattendue.');
    expect(racine.textContent).toContain('Réessayer');
  });
});
