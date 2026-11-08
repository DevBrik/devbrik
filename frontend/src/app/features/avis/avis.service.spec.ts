import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Avis, Ecriture, libelleMotif, MOTIFS, Test } from './avis.modeles';
import { AvisService } from './avis.service';

const AVIS: Avis = {
  id: 1,
  note: 4,
  commentaire: null,
  auteur: null,
  dateCreation: '2026-11-05T14:30:00Z',
  dateModification: '2026-11-05T14:30:00Z',
};

const TEST: Test = {
  id: 2,
  resultat: 'OK',
  environnement: 'Java 21',
  versionCode: 1,
  auteur: null,
  dateTest: '2026-11-05T14:30:00Z',
};

describe('AvisService', () => {
  let service: AvisService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AvisService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('RG8 : un PUT qui répond 201 signale un avis créé', () => {
    let recu: Ecriture<Avis> | undefined;
    service.enregistrerAvis(5, { note: 4, commentaire: 'Utile' }).subscribe((r) => (recu = r));

    const requete = http.expectOne('/api/briques/5/avis');
    expect(requete.request.method).toBe('PUT');
    expect(requete.request.body).toEqual({ note: 4, commentaire: 'Utile' });
    requete.flush(AVIS, { status: 201, statusText: 'Created' });

    expect(recu?.cree).toBe(true);
    expect(recu?.valeur).toEqual(AVIS);
  });

  it('RG8 : un PUT qui répond 200 signale un avis mis à jour', () => {
    let recu: Ecriture<Avis> | undefined;
    service.enregistrerAvis(5, { note: 3 }).subscribe((r) => (recu = r));

    http.expectOne('/api/briques/5/avis').flush(AVIS, { status: 200, statusText: 'OK' });

    expect(recu?.cree).toBe(false);
  });

  it('RG9 : le test se pose puis se retire sur /briques/{id}/test', () => {
    let recu: Ecriture<Test> | undefined;
    service.enregistrerTest(7, { resultat: 'OK', environnement: 'Java 21' }).subscribe((r) => (recu = r));
    const pose = http.expectOne('/api/briques/7/test');
    expect(pose.request.method).toBe('PUT');
    pose.flush(TEST, { status: 201, statusText: 'Created' });
    expect(recu?.cree).toBe(true);

    service.supprimerTest(7).subscribe();
    const retrait = http.expectOne('/api/briques/7/test');
    expect(retrait.request.method).toBe('DELETE');
    retrait.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('RG9 : /moi est lu pour pré-remplir l’avis et le test', () => {
    let recu: unknown;
    service.moi(7).subscribe((r) => (recu = r));

    http.expectOne('/api/briques/7/moi').flush({ monAvis: null, monTest: TEST });

    expect(recu).toEqual({ monAvis: null, monTest: TEST });
  });

  it('RG16 : la liste des avis envoie page et taille', () => {
    service.listerAvis(5, 2, 10).subscribe();

    const requete = http.expectOne((r) => r.url === '/api/briques/5/avis');
    expect(requete.request.params.get('page')).toBe('2');
    expect(requete.request.params.get('taille')).toBe('10');
    requete.flush({ contenu: [], page: 2, taille: 10, totalElements: 0, totalPages: 0 });
  });

  it('RG19 : la suppression d’un avis vise /avis/{id}', () => {
    service.supprimerAvis(9).subscribe();

    const requete = http.expectOne('/api/avis/9');
    expect(requete.request.method).toBe('DELETE');
    requete.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('RG11 : un signalement INAPPROPRIE part en POST', () => {
    service.signaler(5, { motif: 'INAPPROPRIE', details: 'Contenu choquant' }).subscribe();

    const requete = http.expectOne('/api/briques/5/signalements');
    expect(requete.request.method).toBe('POST');
    expect(requete.request.body).toEqual({ motif: 'INAPPROPRIE', details: 'Contenu choquant' });
    requete.flush({ id: 1 }, { status: 201, statusText: 'Created' });
  });

  it('RG11 : la liste administrateur demande OUVERT par défaut, taille 20', () => {
    service.listerSignalements().subscribe();

    const requete = http.expectOne((r) => r.url === '/api/admin/signalements');
    expect(requete.request.params.get('statut')).toBe('OUVERT');
    expect(requete.request.params.get('taille')).toBe('20');
    requete.flush({ contenu: [], page: 0, taille: 20, totalElements: 0, totalPages: 0 });
  });

  it('RG11 : le traitement envoie l’action choisie', () => {
    service.traiterSignalement(9, 'MASQUER_BRIQUE').subscribe();

    const requete = http.expectOne('/api/admin/signalements/9/traitement');
    expect(requete.request.method).toBe('PUT');
    expect(requete.request.body).toEqual({ action: 'MASQUER_BRIQUE' });
    requete.flush({ id: 9 });
  });

  it('U24 : les statistiques viennent de /admin/statistiques', () => {
    service.statistiques().subscribe();

    http.expectOne('/api/admin/statistiques').flush({});
  });

  it('M11 : les briques masquées et la republication passent par les routes du membre 2', () => {
    service.listerBriquesMasquees(1, 20).subscribe();
    const liste = http.expectOne((r) => r.url === '/api/admin/briques');
    expect(liste.request.params.get('statut')).toBe('MASQUEE');
    expect(liste.request.params.get('page')).toBe('1');
    liste.flush({ contenu: [], page: 1, taille: 20, totalElements: 0, totalPages: 0 });

    service.republierBrique(4).subscribe();
    const republication = http.expectOne('/api/admin/briques/4/statut');
    expect(republication.request.method).toBe('PUT');
    expect(republication.request.body).toEqual({ statut: 'PUBLIEE' });
    republication.flush({});
  });
});

describe('MOTIFS', () => {
  it('RG11 : les six motifs sont proposés, dans l’ordre de la maquette', () => {
    expect(MOTIFS.map((m) => m.valeur)).toEqual([
      'INAPPROPRIE', 'CODE_DANGEREUX', 'DROITS_AUTEUR', 'INEXACT', 'SPAM', 'AUTRE',
    ]);
  });

  it('affiche des libellés français', () => {
    expect(libelleMotif('INAPPROPRIE')).toBe('Contenu inapproprié');
    expect(libelleMotif('SPAM')).toBe('Spam ou publicité');
  });
});
