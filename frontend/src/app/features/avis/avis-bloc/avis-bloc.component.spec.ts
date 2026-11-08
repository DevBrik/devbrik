import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Avis } from '../avis.modeles';
import { SocleAvis } from '../avis.socle';
import { AVIS_EXEMPLE, requis, SocleFactice } from '../avis.testing';
import { AvisBlocComponent } from './avis-bloc.component';

function page(contenu: Avis[], totalPages = 1) {
  return { contenu, page: 0, taille: 10, totalElements: contenu.length, totalPages };
}

function monter() {
  TestBed.configureTestingModule({
    imports: [AvisBlocComponent],
    providers: [provideHttpClient(), provideHttpClientTesting(), { provide: SocleAvis, useClass: SocleFactice }],
  });
  const socle = TestBed.inject(SocleAvis) as unknown as SocleFactice;
  const fixture = TestBed.createComponent(AvisBlocComponent);
  fixture.componentRef.setInput('briqueId', 5);
  fixture.detectChanges();
  const http = TestBed.inject(HttpTestingController);
  const lister = (corps: object, statut = 200) => {
    http.expectOne((r) => r.url === '/api/briques/5/avis').flush(corps, { status: statut, statusText: 'x' });
    fixture.detectChanges();
  };
  return { fixture, racine: fixture.nativeElement as HTMLElement, socle, http, lister };
}

describe('AvisBlocComponent', () => {
  it('affiche des étoiles avec un texte « 5 sur 5 » lu par les lecteurs d’écran', () => {
    const { racine, lister } = monter();

    lister(page([{ ...AVIS_EXEMPLE, id: 2, note: 5, auteur: { id: 20, pseudo: 'moussa', nomAffiche: 'Moussa Faye' } }]));

    expect(racine.textContent).toContain('Moussa Faye');
    expect(requis(racine.querySelector('.sr-only')).textContent).toContain('5 sur 5');
    expect(requis(racine.querySelector('.etoiles')).getAttribute('aria-hidden')).toBe('true');
  });

  it('RG19 : Modifier et Supprimer mon avis apparaissent seulement sur sa propre carte', () => {
    const { racine, lister } = monter();

    lister(page([
      { ...AVIS_EXEMPLE, id: 1 },
      { ...AVIS_EXEMPLE, id: 2, auteur: { id: 20, pseudo: 'moussa', nomAffiche: null } },
    ]));

    const textes = Array.from(racine.querySelectorAll('button')).map((b) => b.textContent?.trim());
    expect(textes.filter((t) => t === 'Modifier mon avis')).toHaveLength(1);
    expect(textes.filter((t) => t === 'Supprimer mon avis')).toHaveLength(1);
  });

  it('RG19 : un administrateur peut supprimer l’avis d’un autre', () => {
    const { racine, socle, lister } = monter();
    socle.estAdmin.set(true);

    lister(page([{ ...AVIS_EXEMPLE, id: 2, auteur: { id: 20, pseudo: 'moussa', nomAffiche: 'Moussa Faye' } }]));

    const bouton = requis(racine.querySelector('button.btn-destructif'));
    expect(bouton.getAttribute('aria-label')).toBe('Supprimer l’avis de Moussa Faye');
  });

  it('RG18 : un commentaire contenant du HTML s’affiche tel quel, sans être interprété', () => {
    const { racine, lister } = monter();

    lister(page([{ ...AVIS_EXEMPLE, commentaire: '<script>alert(1)</script>' }]));

    expect(racine.textContent).toContain('<script>alert(1)</script>');
    expect(racine.querySelector('script')).toBeNull();
  });

  it('affiche l’état vide', () => {
    const { racine, lister } = monter();

    lister(page([]));

    expect(racine.textContent).toContain('Aucun avis pour le moment.');
  });

  it('affiche l’erreur avec « Réessayer » puis recharge', () => {
    const { fixture, racine, http, lister } = monter();

    lister({ code: 'ERREUR_INTERNE', message: 'Erreur inattendue.' }, 500);
    expect(racine.textContent).toContain('Erreur inattendue.');

    requis(Array.from(racine.querySelectorAll('button')).find((b) => b.textContent?.trim() === 'Réessayer')).click();
    fixture.detectChanges();
    http.expectOne((r) => r.url === '/api/briques/5/avis').flush(page([]));
  });

  it('RG7 : une brique masquée vue par un tiers (404) affiche un message, sans bouton Réessayer', () => {
    const { racine, lister } = monter();

    lister({ code: 'RESSOURCE_INTROUVABLE', message: 'Brique introuvable.' }, 404);

    expect(racine.textContent).toContain('ne sont pas disponibles');
    expect(racine.textContent).not.toContain('Réessayer');
  });

  it('RG16 : la pagination demande la page suivante', () => {
    const { fixture, racine, http, lister } = monter();
    lister(page([AVIS_EXEMPLE], 3));

    requis(Array.from(racine.querySelectorAll('button')).find((b) => b.textContent?.includes('Suivant'))).click();
    fixture.detectChanges();

    const requete = http.expectOne((r) => r.url === '/api/briques/5/avis');
    expect(requete.request.params.get('page')).toBe('1');
    requete.flush(page([AVIS_EXEMPLE], 3));
  });
});
