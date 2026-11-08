import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Statistiques } from '../../avis.modeles';
import { SocleAvis } from '../../avis.socle';
import { requis, SocleFactice } from '../../avis.testing';
import { StatistiquesComponent } from './statistiques.component';
import { tuilesStatistiques } from './statistiques.tuiles';

const STATS: Statistiques = {
  utilisateurs: 1250,
  briquesPubliees: 486,
  briquesMasquees: 3,
  avis: 920,
  tests: 2450,
  demandesOuvertes: 100,
  demandesResolues: 72,
  signalementsOuverts: 18,
};

function monter() {
  TestBed.configureTestingModule({
    imports: [StatistiquesComponent],
    providers: [
      provideHttpClient(),
      provideHttpClientTesting(),
      provideRouter([]),
      { provide: SocleAvis, useClass: SocleFactice },
    ],
  });
  const fixture = TestBed.createComponent(StatistiquesComponent);
  fixture.detectChanges();
  return {
    fixture,
    racine: fixture.nativeElement as HTMLElement,
    http: TestBed.inject(HttpTestingController),
  };
}

describe('tuilesStatistiques', () => {
  it('construit les six tuiles du dossier 12.7', () => {
    const tuiles = tuilesStatistiques(STATS);

    expect(tuiles.map((t) => t.libelle)).toEqual([
      'Utilisateurs', 'Briques', 'Demandes', 'Avis', 'Tests', 'Signalements ouverts',
    ]);
    expect(tuiles.map((t) => t.valeur)).toEqual([1250, 486, 172, 920, 2450, 18]);
  });

  it('Briques : détail des masquées ; Demandes : ouvertes et résolues', () => {
    const [, briques, demandes] = tuilesStatistiques(STATS);

    expect(briques.detail).toBe('3 masquées');
    expect(demandes.detail?.replace(/\s/g, ' ')).toBe('100 ouvertes · 72 résolues');
  });

  it('accorde au singulier pour 0 et 1', () => {
    const [, briques, demandes] = tuilesStatistiques({
      ...STATS, briquesMasquees: 1, demandesOuvertes: 0, demandesResolues: 1,
    });

    expect(briques.detail).toBe('1 masquée');
    expect(demandes.detail).toBe('0 ouverte · 1 résolue');
  });
});

describe('StatistiquesComponent (M12)', () => {
  it('affiche le titre, le sous-titre et six tuiles', () => {
    const { fixture, racine, http } = monter();

    http.expectOne('/api/admin/statistiques').flush(STATS);
    fixture.detectChanges();

    expect(racine.textContent).toContain('Tableau de bord');
    expect(racine.textContent).toContain('Vue globale de l\'activité de la plateforme.');
    expect(racine.querySelectorAll('li.tuile').length).toBe(6);
    expect(racine.textContent).toContain('Signalements ouverts');
    expect(racine.textContent).toContain('3 masquées');
    expect(racine.querySelector('canvas, svg')).toBeNull();
  });

  it('chargement : squelette masqué aux lecteurs d’écran et annonce « Chargement… »', () => {
    const { racine, http } = monter();

    expect(racine.querySelector('[aria-hidden="true"].tuiles')).not.toBeNull();
    expect(racine.querySelector('[role="status"]')?.textContent).toContain('Chargement des statistiques');
    http.expectOne('/api/admin/statistiques').flush(STATS);
  });

  it('erreur 403 : message du serveur, pas de bouton « Réessayer »', () => {
    const { fixture, racine, http } = monter();

    http
      .expectOne('/api/admin/statistiques')
      .flush(
        { statut: 403, code: 'ACCES_REFUSE', message: 'Accès réservé aux administrateurs.' },
        { status: 403, statusText: 'Forbidden' },
      );
    fixture.detectChanges();

    expect(requis(racine.querySelector('[role="alert"]')).textContent).toContain('Accès réservé aux administrateurs.');
    expect(racine.querySelector('button')).toBeNull();
  });

  it('erreur réseau : « Réessayer » relance la requête', () => {
    const { fixture, racine, http } = monter();
    http.expectOne('/api/admin/statistiques').error(new ProgressEvent('error'));
    fixture.detectChanges();

    requis(racine.querySelector<HTMLButtonElement>('button')).click();

    http.expectOne('/api/admin/statistiques').flush(STATS);
    fixture.detectChanges();
    expect(racine.querySelectorAll('li.tuile').length).toBe(6);
  });
});
