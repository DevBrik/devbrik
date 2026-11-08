import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { PageReponse } from '../../../../core/api/modeles';
import { BriqueResumeAdmin, Signalement } from '../../avis.modeles';
import { SocleAvis } from '../../avis.socle';
import { requis, SocleFactice } from '../../avis.testing';
import { SignalementsComponent } from './signalements.component';

const signalement = (id: number, extra: Partial<Signalement> = {}): Signalement => ({
  id,
  motif: 'CODE_DANGEREUX',
  details: 'Le contenu semble contenir une opération non documentée.',
  statut: 'OUVERT',
  actionTraitement: null,
  dateCreation: '2026-11-05T14:30:00Z',
  dateTraitement: null,
  brique: {
    id: 7,
    titre: 'Code de paiement',
    statut: 'PUBLIEE',
    auteur: { id: 3, pseudo: 'papa.diao', nomAffiche: 'Papa Birane DIAO' },
  },
  auteur: { id: 4, pseudo: 'ndiaye', nomAffiche: 'Ndiémé NDIAYE' },
  ...extra,
});

const brique = (id: number, titre = 'Montant en lettres'): BriqueResumeAdmin => ({
  id,
  titre,
  resume: 'Convertit un montant en lettres.',
  licence: 'MIT',
  statut: 'MASQUEE',
  versionCode: 2,
  dateCreation: '2026-10-18T09:00:00Z',
  dateModification: '2026-10-22T09:00:00Z',
  categorie: { slug: 'formats-devises', nom: 'Formats, dates et devises' },
  langage: { slug: 'java', nom: 'Java' },
  auteur: { id: 3, pseudo: 'papa.diao', nomAffiche: 'Papa Birane DIAO' },
  noteMoyenne: 4.8,
  nombreAvis: 12,
  testsOk: 2,
  testsKo: 0,
  derniereVerification: '2026-10-22T09:00:00Z',
});

const page = <T>(contenu: T[], totalPages = 1): PageReponse<T> => ({
  contenu,
  page: 0,
  taille: 20,
  totalElements: contenu.length,
  totalPages,
});

function monter(confirmation = true) {
  const dialogues: unknown[] = [];
  TestBed.configureTestingModule({
    imports: [SignalementsComponent],
    providers: [
      provideHttpClient(),
      provideHttpClientTesting(),
      provideRouter([]),
      { provide: SocleAvis, useClass: SocleFactice },
      {
        provide: MatDialog,
        useValue: {
          open: (_composant: unknown, config: unknown) => {
            dialogues.push(config);
            return { afterClosed: () => of(confirmation) };
          },
        },
      },
    ],
  });
  const fixture = TestBed.createComponent(SignalementsComponent);
  fixture.detectChanges();
  const racine = fixture.nativeElement as HTMLElement;
  const http = TestBed.inject(HttpTestingController);
  const socle = TestBed.inject(SocleAvis) as unknown as SocleFactice;
  return {
    fixture,
    racine,
    http,
    socle,
    dialogues,
    listeSignalements: () => http.expectOne((r) => r.url === '/api/admin/signalements'),
    listeMasquees: () => http.expectOne((r) => r.url === '/api/admin/briques'),
    bouton: (texte: string) =>
      requis(
        Array.from(racine.querySelectorAll<HTMLElement>('button, a')).find((b) => b.textContent?.trim() === texte),
      ),
    charger: (signalements: Signalement[], masquees: BriqueResumeAdmin[] = []) => {
      http.expectOne((r) => r.url === '/api/admin/signalements').flush(page(signalements));
      http.expectOne((r) => r.url === '/api/admin/briques').flush(page(masquees));
      fixture.detectChanges();
    },
  };
}

describe('SignalementsComponent (M11)', () => {
  it('charge les signalements OUVERT (taille 20) et les briques MASQUEE', () => {
    const { listeSignalements, listeMasquees } = monter();

    const s = listeSignalements();
    expect(s.request.params.get('statut')).toBe('OUVERT');
    expect(s.request.params.get('page')).toBe('0');
    expect(s.request.params.get('taille')).toBe('20');
    const m = listeMasquees();
    expect(m.request.params.get('statut')).toBe('MASQUEE');
    s.flush(page([]));
    m.flush(page([]));
  });

  it('affiche une carte par signalement et invite à choisir', () => {
    const { racine, charger } = monter();

    charger([signalement(1), signalement(2, { motif: 'SPAM' })]);

    expect(racine.querySelectorAll('avis-carte-signalement').length).toBe(2);
    expect(racine.textContent).toContain('Signalements à examiner');
    expect(racine.textContent).toContain('Choisissez un signalement dans la liste');
    expect(racine.textContent).not.toContain('Poser votre curseur');
  });

  it('« Examiner » affiche le panneau avec brique, auteur, signalé par et description', () => {
    const { fixture, racine, charger, bouton } = monter();
    charger([signalement(1)]);

    bouton('Examiner').click();
    fixture.detectChanges();

    const panneau = requis(racine.querySelector('avis-detail-signalement')).textContent ?? '';
    expect(panneau).toContain('Code dangereux');
    expect(panneau).toContain('Brique : Code de paiement');
    expect(panneau).toContain('Auteur : Papa Birane DIAO');
    expect(panneau).toContain('Signalé par : Ndiémé NDIAYE');
    expect(panneau).toContain('Le contenu semble contenir une opération non documentée.');
    expect(requis(racine.querySelector('button[aria-current="true"]'))).toBeTruthy();
    expect(bouton('Consulter la brique').getAttribute('href')).toBe('/briques/7');
  });

  it('description absente : « Aucune description »', () => {
    const { fixture, racine, charger, bouton } = monter();
    charger([signalement(1, { details: null })]);

    bouton('Examiner').click();
    fixture.detectChanges();

    expect(racine.querySelector('avis-detail-signalement')?.textContent).toContain('Aucune description');
  });

  it('texte brut : une description contenant du HTML n’est jamais interprétée', () => {
    const html = '<img src=x onerror=alert(1)><b>gras</b>';
    const { fixture, racine, charger, bouton } = monter();
    charger([signalement(1, { details: html })]);

    bouton('Examiner').click();
    fixture.detectChanges();

    const panneau = requis(racine.querySelector('avis-detail-signalement'));
    expect(panneau.textContent).toContain(html);
    expect(panneau.querySelector('img')).toBeNull();
    expect(panneau.querySelector('b')).toBeNull();
  });

  it('RG11 : « Conserver la brique » envoie CONSERVER sans confirmation, puis rafraîchit', () => {
    const { fixture, http, socle, dialogues, charger, bouton } = monter();
    charger([signalement(1)]);
    bouton('Examiner').click();
    fixture.detectChanges();

    bouton('Conserver la brique').click();
    fixture.detectChanges();

    const requete = http.expectOne('/api/admin/signalements/1/traitement');
    expect(requete.request.method).toBe('PUT');
    expect(requete.request.body).toEqual({ action: 'CONSERVER' });
    expect(dialogues.length).toBe(0);
    requete.flush(signalement(1, { statut: 'TRAITE', actionTraitement: 'CONSERVER' }));
    expect(socle.messagesSucces).toEqual(['Signalement traité : la brique est conservée.']);
    // la liste et les briques masquées sont relues
    http.expectOne((r) => r.url === '/api/admin/signalements').flush(page([]));
    http.expectOne((r) => r.url === '/api/admin/briques').flush(page([]));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Aucun signalement à examiner.');
  });

  it('RG11 : « Masquer la brique » demande confirmation puis envoie MASQUER_BRIQUE', () => {
    const { fixture, http, socle, dialogues, charger, bouton } = monter(true);
    charger([signalement(1)]);
    bouton('Examiner').click();
    fixture.detectChanges();

    bouton('Masquer la brique').click();

    expect(dialogues.length).toBe(1);
    expect((dialogues[0] as { data: { titre: string } }).data.titre).toBe('Masquer cette brique ?');
    const requete = http.expectOne('/api/admin/signalements/1/traitement');
    expect(requete.request.body).toEqual({ action: 'MASQUER_BRIQUE' });
    requete.flush(signalement(1, { statut: 'TRAITE', actionTraitement: 'MASQUER_BRIQUE' }));
    expect(socle.messagesSucces).toEqual(['Signalement traité : la brique est masquée.']);
    http.expectOne((r) => r.url === '/api/admin/signalements').flush(page([]));
    http.expectOne((r) => r.url === '/api/admin/briques').flush(page([brique(7, 'Code de paiement')]));
  });

  it('confirmation refusée : aucune requête n’est envoyée', () => {
    const { fixture, http, charger, bouton } = monter(false);
    charger([signalement(1)]);
    bouton('Examiner').click();
    fixture.detectChanges();

    bouton('Masquer la brique').click();

    http.expectNone('/api/admin/signalements/1/traitement');
  });

  it('échec du traitement : le message du serveur est notifié et la sélection reste', () => {
    const { fixture, racine, http, socle, charger, bouton } = monter();
    charger([signalement(1)]);
    bouton('Examiner').click();
    fixture.detectChanges();

    bouton('Conserver la brique').click();
    http
      .expectOne('/api/admin/signalements/1/traitement')
      .flush(
        { statut: 403, code: 'ACCES_REFUSE', message: 'Action réservée aux administrateurs.' },
        { status: 403, statusText: 'Forbidden' },
      );
    fixture.detectChanges();

    expect(socle.messagesErreur).toEqual(['Action réservée aux administrateurs.']);
    expect(racine.querySelector('avis-detail-signalement')).not.toBeNull();
    expect(bouton('Conserver la brique').hasAttribute('disabled')).toBe(false);
  });

  it('briques masquées : Voir (lien) et Republier sont visibles en permanence', () => {
    const { racine, charger, bouton } = monter();

    charger([], [brique(9)]);

    expect(racine.textContent).toContain('Briques masquées');
    expect(bouton('Voir').getAttribute('href')).toBe('/briques/9');
    expect(bouton('Republier')).toBeTruthy();
    expect(racine.textContent).toContain('Masquée');
    expect(racine.textContent).toContain('Testé OK');
    expect(racine.textContent).toContain('4,8 sur 5 (12 avis)');
  });

  it('« Republier » envoie PUBLIEE puis relit les briques masquées', () => {
    const { fixture, http, socle, charger, bouton } = monter();
    charger([], [brique(9)]);

    bouton('Republier').click();
    fixture.detectChanges();

    const requete = http.expectOne('/api/admin/briques/9/statut');
    expect(requete.request.method).toBe('PUT');
    expect(requete.request.body).toEqual({ statut: 'PUBLIEE' });
    requete.flush({});
    expect(socle.messagesSucces).toEqual(['La brique « Montant en lettres » est republiée.']);
    http.expectOne((r) => r.url === '/api/admin/briques').flush(page([]));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Aucune brique masquée.');
  });

  it('listes vides : messages explicites, pas de zone blanche', () => {
    const { racine, charger } = monter();

    charger([], []);

    expect(racine.textContent).toContain('Aucun signalement à examiner.');
    expect(racine.textContent).toContain('Aucune brique masquée.');
  });

  it('erreur réseau : message, « Réessayer » relance la requête', () => {
    const { fixture, racine, http, listeMasquees, bouton } = monter();
    http.expectOne((r) => r.url === '/api/admin/signalements').error(new ProgressEvent('error'));
    listeMasquees().flush(page([]));
    fixture.detectChanges();

    expect(racine.querySelector('[role="alert"]')?.textContent).toContain('Connexion impossible');

    bouton('Réessayer').click();
    const relance = http.expectOne((r) => r.url === '/api/admin/signalements');
    relance.flush(page([signalement(1)]));
    fixture.detectChanges();

    expect(racine.querySelectorAll('avis-carte-signalement').length).toBe(1);
  });

  it('pagination : « Suivant » demande la page 1', () => {
    const { fixture, http, racine, bouton } = monter();
    http.expectOne((r) => r.url === '/api/admin/signalements').flush(page([signalement(1)], 3));
    http.expectOne((r) => r.url === '/api/admin/briques').flush(page([]));
    fixture.detectChanges();
    expect(racine.textContent).toContain('Page 1 sur 3');

    bouton('Suivant →').click();

    const requete = http.expectOne((r) => r.url === '/api/admin/signalements');
    expect(requete.request.params.get('page')).toBe('1');
    requete.flush(page([signalement(2)], 3));
  });
});
