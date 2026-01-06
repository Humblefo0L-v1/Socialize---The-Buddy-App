import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BuddyMarker } from './buddy-marker';

describe('BuddyMarker', () => {
  let component: BuddyMarker;
  let fixture: ComponentFixture<BuddyMarker>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BuddyMarker]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BuddyMarker);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
