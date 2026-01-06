import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EventList } from './event-list';

describe('EventList', () => {
  let component: EventList;
  let fixture: ComponentFixture<EventList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EventList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
