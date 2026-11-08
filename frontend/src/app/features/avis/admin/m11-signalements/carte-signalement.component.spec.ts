import { TestBed } from '@angular/core/testing';
import { Signalement } from '../../avis.modeles';
import { requis } from '../../avis.testing';
import { CarteSignalementComponent } from './carte-signalement.component';

const SIGNALEMENT: Signalement = {
  id: 5,
  motif: 'INAPPROPRIE',
  details: null,
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
};

function monter(signalement: Signalement, selectionnee = false) {
  TestBed.configureTestingModule({ imports: [CarteSignalementComponent] });
  const fixture = TestBed.createComponent(CarteSignalementComponent);
  fixture.componentRef.setInput('signalement', signalement);
  fixture.componentRef.setInput('selectionnee', selectionnee);
  fixture.detectChanges();
  const racine = fixture.nativeElement as HTMLElement;
  const examines: number[] = [];
  fixture.componentInstance.examiner.subscribe((id) => examines.push(id));
  return { fixture, racine, examines, bouton: () => requis(racine.querySelector('button')) };
}

describe('CarteSignalementComponent', () => {
  it('affiche le titre de la brique, le motif lisible, qui a signalé et la date', () => {
    const { racine } = monter(SIGNALEMENT);

    expect(racine.querySelector('h3')?.textContent).toContain('Code de paiement');
    expect(racine.textContent).toContain('Contenu inapproprié');
    expect(racine.textContent).toContain('Signalé par Ndiémé NDIAYE');
    expect(racine.querySelector('time')?.getAttribute('datetime')).toBe('2026-11-05T14:30:00Z');
  });

  it('émet l’identifiant du signalement quand on clique sur « Examiner »', () => {
    const { bouton, examines } = monter(SIGNALEMENT);

    bouton().click();

    expect(examines).toEqual([5]);
    expect(bouton().textContent?.trim()).toBe('Examiner');
  });

  it('carte non choisie : pas d’aria-current ni de mention « Sélectionné »', () => {
    const { racine, bouton } = monter(SIGNALEMENT, false);

    expect(bouton().hasAttribute('aria-current')).toBe(false);
    expect(racine.textContent).not.toContain('Sélectionné');
  });

  it('carte choisie : aria-current et mention « Sélectionné » (jamais la couleur seule)', () => {
    const { racine, bouton } = monter(SIGNALEMENT, true);

    expect(bouton().getAttribute('aria-current')).toBe('true');
    expect(racine.textContent).toContain('Sélectionné');
    expect(racine.querySelector('article')?.classList.contains('selectionnee')).toBe(true);
  });

  it('le nom accessible du bouton contient son texte visible et nomme la brique', () => {
    const { bouton } = monter(SIGNALEMENT);

    expect(bouton().getAttribute('aria-label')).toBe('Examiner le signalement de la brique Code de paiement');
  });

  it('brique absente : « Brique indisponible » au lieu d’une erreur', () => {
    const { racine } = monter({ ...SIGNALEMENT, brique: null });

    expect(racine.querySelector('h3')?.textContent).toContain('Brique indisponible');
  });

  it('texte brut : un titre contenant du HTML est affiché tel quel', () => {
    const titre = '<script>alert(1)</script><img src=x onerror=alert(2)>';
    const { racine } = monter({ ...SIGNALEMENT, brique: { ...requis(SIGNALEMENT.brique), titre } });

    expect(racine.querySelector('h3')?.textContent).toContain(titre);
    expect(racine.querySelector('script')).toBeNull();
    expect(racine.querySelector('img')).toBeNull();
  });
});
